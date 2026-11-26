package br.com.fiap.copiloto.algoritmo;

import br.com.fiap.copiloto.model.ArestaMapa;
import br.com.fiap.copiloto.model.NoMapa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Store floor plan as an undirected weighted graph stored in adjacency lists.
 * Shortest paths use Dijkstra with a priority queue (edge weights are meters).
 */
public class GrafoLoja {

    private final Map<Long, NoMapa> nos = new HashMap<>();
    private final Map<Long, List<ArestaMapa>> adjacencias = new HashMap<>();

    public GrafoLoja(List<NoMapa> listaNos, List<ArestaMapa> arestas) {
        for (NoMapa no : listaNos) {
            nos.put(no.id(), no);
            adjacencias.put(no.id(), new ArrayList<>());
        }
        for (ArestaMapa aresta : arestas) {
            adjacencias.get(aresta.origem()).add(aresta);
            adjacencias.get(aresta.destino()).add(new ArestaMapa(aresta.destino(), aresta.origem(), aresta.metros()));
        }
    }

    public record CaminhosMinimos(Map<Long, Double> distancias, Map<Long, Long> anteriores) {

        public List<Long> caminhoAte(long destino) {
            List<Long> caminho = new ArrayList<>();
            Long atual = destino;
            while (atual != null) {
                caminho.add(atual);
                atual = anteriores.get(atual);
            }
            Collections.reverse(caminho);
            return caminho;
        }

        public double distanciaAte(long destino) {
            return distancias.getOrDefault(destino, Double.POSITIVE_INFINITY);
        }
    }

    private record Visita(long no, double distancia) {
    }

    public CaminhosMinimos dijkstra(long origem) {
        Map<Long, Double> distancias = new HashMap<>();
        Map<Long, Long> anteriores = new HashMap<>();
        PriorityQueue<Visita> fila = new PriorityQueue<>((a, b) -> Double.compare(a.distancia(), b.distancia()));

        distancias.put(origem, 0.0);
        fila.add(new Visita(origem, 0.0));

        while (!fila.isEmpty()) {
            Visita visita = fila.poll();
            if (visita.distancia() > distancias.getOrDefault(visita.no(), Double.POSITIVE_INFINITY)) {
                continue;
            }
            for (ArestaMapa aresta : adjacencias.getOrDefault(visita.no(), List.of())) {
                double novaDistancia = visita.distancia() + aresta.metros();
                if (novaDistancia < distancias.getOrDefault(aresta.destino(), Double.POSITIVE_INFINITY)) {
                    distancias.put(aresta.destino(), novaDistancia);
                    anteriores.put(aresta.destino(), visita.no());
                    fila.add(new Visita(aresta.destino(), novaDistancia));
                }
            }
        }
        return new CaminhosMinimos(distancias, anteriores);
    }

    public NoMapa no(long id) {
        return nos.get(id);
    }

    public NoMapa noPorCodigo(String codigo) {
        return nos.values().stream()
                .filter(n -> n.codigo().equals(codigo))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Map node not found: " + codigo));
    }

    public List<NoMapa> nos() {
        return new ArrayList<>(nos.values());
    }

    public List<ArestaMapa> arestasUnicas() {
        List<ArestaMapa> unicas = new ArrayList<>();
        for (List<ArestaMapa> lista : adjacencias.values()) {
            for (ArestaMapa aresta : lista) {
                if (aresta.origem() < aresta.destino()) {
                    unicas.add(aresta);
                }
            }
        }
        return unicas;
    }
}
