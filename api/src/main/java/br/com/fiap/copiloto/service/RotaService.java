package br.com.fiap.copiloto.service;

import br.com.fiap.copiloto.algoritmo.GrafoLoja;
import br.com.fiap.copiloto.algoritmo.MelhorOrdemVisita;
import br.com.fiap.copiloto.algoritmo.Ordenacao;
import br.com.fiap.copiloto.dto.MapaDTO;
import br.com.fiap.copiloto.dto.ParadaDTO;
import br.com.fiap.copiloto.dto.PontoDTO;
import br.com.fiap.copiloto.dto.RotaDTO;
import br.com.fiap.copiloto.exception.RegraNegocioException;
import br.com.fiap.copiloto.model.Corredor;
import br.com.fiap.copiloto.model.NoMapa;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Plans the walk through the store: Dijkstra gives the distance between every pair of
 * stops, then Held-Karp picks the visiting order with the shortest total walk.
 */
public class RotaService {

    /** Average walking speed with a shopping cart, in meters per minute. */
    private static final double METROS_POR_MINUTO = 50.0;
    /** Time spent picking items at each stop, in minutes. */
    private static final double MINUTOS_POR_PARADA = 1.5;

    private final GrafoLoja grafo;
    private final CatalogoService catalogo;

    public RotaService(GrafoLoja grafo, CatalogoService catalogo) {
        this.grafo = grafo;
        this.catalogo = catalogo;
    }

    /**
     * @param skusPorCorredor corridor number -> SKUs to pick there
     */
    public RotaDTO planejar(Map<Integer, List<String>> skusPorCorredor) {
        long inicio = grafo.noPorCodigo("ENTRADA").id();
        long caixa = grafo.noPorCodigo("CAIXA").id();

        List<Corredor> corredores = new ArrayList<>();
        for (Integer numero : skusPorCorredor.keySet()) {
            corredores.add(catalogo.corredor(numero));
        }
        int n = corredores.size();
        if (n > MelhorOrdemVisita.MAX_PARADAS) {
            throw new RegraNegocioException("A rota suporta no máximo " + MelhorOrdemVisita.MAX_PARADAS + " corredores.");
        }

        Map<Long, GrafoLoja.CaminhosMinimos> minimosPorOrigem = new HashMap<>();
        minimosPorOrigem.put(inicio, grafo.dijkstra(inicio));
        for (Corredor corredor : corredores) {
            minimosPorOrigem.put(corredor.idNo(), grafo.dijkstra(corredor.idNo()));
        }
        double[] doInicio = new double[n];
        double[] ateFim = new double[n];
        double[][] entreParadas = new double[n][n];
        for (int i = 0; i < n; i++) {
            long noI = corredores.get(i).idNo();
            doInicio[i] = minimosPorOrigem.get(inicio).distanciaAte(noI);
            ateFim[i] = minimosPorOrigem.get(noI).distanciaAte(caixa);
            for (int j = 0; j < n; j++) {
                entreParadas[i][j] = minimosPorOrigem.get(noI).distanciaAte(corredores.get(j).idNo());
            }
        }

        List<ParadaDTO> paradas = new ArrayList<>();
        List<Long> caminho = new ArrayList<>();
        caminho.add(inicio);
        double total = 0;
        long atual = inicio;
        for (int indice : MelhorOrdemVisita.resolver(doInicio, entreParadas, ateFim)) {
            Corredor corredor = corredores.get(indice);
            GrafoLoja.CaminhosMinimos minimos = minimosPorOrigem.get(atual);
            List<Long> trecho = minimos.caminhoAte(corredor.idNo());
            caminho.addAll(trecho.subList(1, trecho.size()));
            double trechoMetros = minimos.distanciaAte(corredor.idNo());
            total += trechoMetros;
            NoMapa no = grafo.no(corredor.idNo());
            paradas.add(new ParadaDTO(paradas.size() + 1, corredor.numero(), corredor.secao().nome(),
                    corredor.secao().cor(), no.x(), no.y(), skusPorCorredor.get(corredor.numero()),
                    arredondar(trechoMetros)));
            atual = corredor.idNo();
        }

        GrafoLoja.CaminhosMinimos ateCaixa = minimosPorOrigem.get(atual);
        List<Long> trechoFinal = ateCaixa.caminhoAte(caixa);
        caminho.addAll(trechoFinal.subList(1, trechoFinal.size()));
        total += ateCaixa.distanciaAte(caixa);

        int minutos = (int) Math.ceil(total / METROS_POR_MINUTO + paradas.size() * MINUTOS_POR_PARADA);
        return new RotaDTO(paradas, paraPontos(caminho), arredondar(total), minutos,
                arredondar(distanciaEmOrdemNumerica(skusPorCorredor, inicio, caixa)));
    }

    /** Baseline for comparison: walking the corridors in numeric order, as a customer without guidance would. */
    private double distanciaEmOrdemNumerica(Map<Integer, List<String>> skusPorCorredor, long inicio, long caixa) {
        List<Integer> numeros = Ordenacao.mergeSort(new ArrayList<>(skusPorCorredor.keySet()), Comparator.naturalOrder());
        double total = 0;
        long atual = inicio;
        for (Integer numero : numeros) {
            long destino = catalogo.corredor(numero).idNo();
            total += grafo.dijkstra(atual).distanciaAte(destino);
            atual = destino;
        }
        return total + grafo.dijkstra(atual).distanciaAte(caixa);
    }

    public MapaDTO mapa() {
        List<MapaDTO.NoDTO> nos = grafo.nos().stream()
                .map(n -> new MapaDTO.NoDTO(n.id(), n.codigo(), n.tipo(), n.x(), n.y()))
                .toList();
        List<MapaDTO.ArestaDTO> arestas = grafo.arestasUnicas().stream()
                .map(a -> new MapaDTO.ArestaDTO(a.origem(), a.destino(), a.metros()))
                .toList();
        List<MapaDTO.CorredorDTO> corredores = catalogo.corredores().stream()
                .map(c -> {
                    NoMapa no = grafo.no(c.idNo());
                    return new MapaDTO.CorredorDTO(c.numero(), c.secao().nome(), c.secao().cor(), no.x(), no.y());
                })
                .toList();
        return new MapaDTO(nos, arestas, corredores);
    }

    private List<PontoDTO> paraPontos(List<Long> caminho) {
        List<PontoDTO> pontos = new ArrayList<>();
        for (Long id : caminho) {
            NoMapa no = grafo.no(id);
            pontos.add(new PontoDTO(no.x(), no.y()));
        }
        return pontos;
    }

    private static double arredondar(double valor) {
        return Math.round(valor * 10) / 10.0;
    }
}
