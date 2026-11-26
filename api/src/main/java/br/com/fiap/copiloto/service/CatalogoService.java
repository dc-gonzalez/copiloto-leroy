package br.com.fiap.copiloto.service;

import br.com.fiap.copiloto.algoritmo.BuscaBinaria;
import br.com.fiap.copiloto.algoritmo.Ordenacao;
import br.com.fiap.copiloto.dao.CatalogoDAO;
import br.com.fiap.copiloto.dto.ProdutoDTO;
import br.com.fiap.copiloto.exception.ProdutoIndisponivelException;
import br.com.fiap.copiloto.exception.RecursoNaoEncontradoException;
import br.com.fiap.copiloto.exception.RegraNegocioException;
import br.com.fiap.copiloto.model.Complemento;
import br.com.fiap.copiloto.model.Corredor;
import br.com.fiap.copiloto.model.Produto;
import br.com.fiap.copiloto.model.TipoDisponibilidade;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Catalog kept in memory: the store has a few thousand SKUs at most, and every
 * assistant answer needs to look products up many times.
 */
public class CatalogoService {

    private final CatalogoDAO dao;
    private final List<Produto> produtosPorSku;
    private final Map<Long, Produto> produtosPorId = new HashMap<>();
    private final Map<Integer, Corredor> corredoresPorNumero = new HashMap<>();
    private final Map<Long, List<Long>> similares = new HashMap<>();
    private final Map<Long, List<Complemento>> complementos = new HashMap<>();

    public CatalogoService(CatalogoDAO dao) {
        this.dao = dao;
        for (Corredor corredor : dao.listarCorredores()) {
            corredoresPorNumero.put(corredor.numero(), corredor);
        }
        List<Produto> produtos = dao.listarProdutos();
        this.produtosPorSku = Ordenacao.mergeSort(produtos, Comparator.comparing(Produto::getSku));
        for (Produto produto : produtos) {
            produtosPorId.put(produto.getId(), produto);
        }
        for (long[] par : dao.listarSimilares()) {
            similares.computeIfAbsent(par[0], k -> new ArrayList<>()).add(par[1]);
        }
        for (Complemento complemento : dao.listarComplementos()) {
            complementos.computeIfAbsent(complemento.idProduto(), k -> new ArrayList<>()).add(complemento);
        }
    }

    public Optional<Produto> buscarPorSku(String sku) {
        if (sku == null) {
            return Optional.empty();
        }
        int indice = BuscaBinaria.buscar(produtosPorSku, sku.trim().toUpperCase(), Produto::getSku);
        return indice < 0 ? Optional.empty() : Optional.of(produtosPorSku.get(indice));
    }

    public Produto obterPorSku(String sku) {
        return buscarPorSku(sku)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto " + sku + " não encontrado."));
    }

    public Produto obterPorId(long id) {
        Produto produto = produtosPorId.get(id);
        if (produto == null) {
            throw new RecursoNaoEncontradoException("Produto não encontrado.");
        }
        return produto;
    }

    public List<Produto> todos() {
        return produtosPorSku;
    }

    public Corredor corredor(int numero) {
        Corredor corredor = corredoresPorNumero.get(numero);
        if (corredor == null) {
            throw new RecursoNaoEncontradoException("Corredor " + numero + " não existe nesta loja.");
        }
        return corredor;
    }

    public Corredor corredorPorId(int idCorredor) {
        return corredoresPorNumero.values().stream()
                .filter(c -> c.id() == idCorredor)
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Corredor não encontrado."));
    }

    public List<Corredor> corredores() {
        return Ordenacao.mergeSort(new ArrayList<>(corredoresPorNumero.values()), Comparator.comparingInt(Corredor::numero));
    }

    public List<Complemento> complementosDe(Produto produto) {
        return complementos.getOrDefault(produto.getId(), List.of());
    }

    /**
     * Ensures the product can be picked from the shelf now.
     *
     * @throws ProdutoIndisponivelException when the system shows stock but the shelf is empty
     */
    public Produto garantirNaGondola(Produto produto) throws ProdutoIndisponivelException {
        if (produto.estaEmRuptura()) {
            throw new ProdutoIndisponivelException(produto);
        }
        return produto;
    }

    /** Equivalent products the customer can take right now: own brand first, then cheapest. */
    public List<Produto> alternativasDisponiveis(Produto produto) {
        List<Produto> disponiveis = new ArrayList<>();
        for (Long id : similares.getOrDefault(produto.getId(), List.of())) {
            Produto similar = produtosPorId.get(id);
            if (similar != null && similar.podeLevarAgora()) {
                disponiveis.add(similar);
            }
        }
        Comparator<Produto> marcaPropriaPrimeiro = Comparator.comparing(p -> !p.isMarcaPropria());
        return Ordenacao.mergeSort(disponiveis, marcaPropriaPrimeiro.thenComparing(Produto::getPreco));
    }

    public synchronized void reporGondola(Produto produto) {
        if (produto.getDisponibilidade() != TipoDisponibilidade.PRONTA_ENTREGA) {
            throw new RegraNegocioException("Produto de mostruário não fica na prateleira.");
        }
        if (produto.getEstoqueSistema() == 0) {
            throw new RegraNegocioException("Não há estoque no depósito para repor este produto.");
        }
        produto.reporGondola(dao.reporGondola(produto.getId()));
    }

    public synchronized void registrarGondolaVazia(Produto produto) {
        if (produto.getEstoqueGondola() > 0) {
            dao.zerarGondola(produto.getId());
        }
        produto.registrarGondolaVazia();
    }

    /** Keyword search over name, keywords, brand and section, ignoring accents and plurals. */
    public List<Produto> pesquisar(String texto, int limite) {
        List<String> termos = termos(texto);
        if (termos.isEmpty()) {
            return List.of();
        }
        record Pontuado(Produto produto, int pontos) {
        }
        List<Pontuado> encontrados = new ArrayList<>();
        for (Produto produto : produtosPorSku) {
            String nome = normalizar(produto.getNome());
            String chaves = normalizar(produto.getPalavrasChave());
            String extra = normalizar(produto.getMarca() + " " + corredor(produto.getNumeroCorredor()).secao().nome());
            int pontos = 0;
            for (String termo : termos) {
                if (nome.contains(termo)) {
                    pontos += 3;
                }
                if (chaves.contains(termo)) {
                    pontos += 2;
                }
                if (extra.contains(termo)) {
                    pontos += 1;
                }
            }
            if (produto.getSku().equalsIgnoreCase(texto.trim())) {
                pontos += 10;
            }
            if (pontos > 0) {
                encontrados.add(new Pontuado(produto, pontos));
            }
        }
        Comparator<Pontuado> porPontos = Comparator.comparingInt(Pontuado::pontos).reversed();
        List<Pontuado> ordenados = Ordenacao.mergeSort(encontrados,
                porPontos.thenComparing(p -> !p.produto().isMarcaPropria()));
        return ordenados.stream().limit(limite).map(Pontuado::produto).toList();
    }

    public ProdutoDTO paraDTO(Produto produto) {
        Corredor corredor = corredor(produto.getNumeroCorredor());
        return new ProdutoDTO(
                produto.getSku(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getMarca(),
                produto.isMarcaPropria(),
                produto.getPreco(),
                produto.getUnidade(),
                corredor.numero(),
                corredor.secao().nome(),
                corredor.secao().cor(),
                produto.getPrateleira(),
                produto.getTipoAtendimento().name(),
                produto.getTipoAtendimento().getDescricao(),
                produto.getDisponibilidade().name(),
                produto.podeLevarAgora(),
                produto.estaEmRuptura(),
                produto.getEstoqueGondola(),
                produto.getEstoqueSistema(),
                produto.orientacaoCompra());
    }

    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase();
    }

    private static List<String> termos(String texto) {
        List<String> termos = new ArrayList<>();
        for (String parte : normalizar(texto).split("[^a-z0-9]+")) {
            if (parte.length() < 3) {
                continue;
            }
            String termo = parte.length() > 4 && parte.endsWith("s") ? parte.substring(0, parte.length() - 1) : parte;
            termos.add(termo);
        }
        return termos;
    }
}
