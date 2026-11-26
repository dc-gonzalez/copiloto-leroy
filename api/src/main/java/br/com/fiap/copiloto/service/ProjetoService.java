package br.com.fiap.copiloto.service;

import br.com.fiap.copiloto.algoritmo.Ordenacao;
import br.com.fiap.copiloto.dao.ProjetoDAO;
import br.com.fiap.copiloto.dto.ItemRequest;
import br.com.fiap.copiloto.dto.ItemSugeridoDTO;
import br.com.fiap.copiloto.dto.ProjetoDTO;
import br.com.fiap.copiloto.dto.ProjetoRequest;
import br.com.fiap.copiloto.dto.RotaDTO;
import br.com.fiap.copiloto.exception.RecursoNaoEncontradoException;
import br.com.fiap.copiloto.exception.RegraNegocioException;
import br.com.fiap.copiloto.model.ItemProjeto;
import br.com.fiap.copiloto.model.Produto;
import br.com.fiap.copiloto.model.Projeto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ProjetoService {

    private static final int MAX_ITENS = 40;

    private final ProjetoDAO dao;
    private final CatalogoService catalogo;
    private final RotaService rotas;

    public ProjetoService(ProjetoDAO dao, CatalogoService catalogo, RotaService rotas) {
        this.dao = dao;
        this.catalogo = catalogo;
        this.rotas = rotas;
    }

    public ProjetoDTO criar(ProjetoRequest requisicao) {
        if (requisicao == null || requisicao.itens() == null || requisicao.itens().isEmpty()) {
            throw new RegraNegocioException("A lista precisa ter pelo menos um produto.");
        }
        if (requisicao.itens().size() > MAX_ITENS) {
            throw new RegraNegocioException("A lista pode ter no máximo " + MAX_ITENS + " produtos.");
        }
        Map<Long, ItemProjeto> itens = new LinkedHashMap<>();
        for (ItemRequest item : requisicao.itens()) {
            Produto produto = catalogo.obterPorSku(item.sku());
            if (item.quantidade() < 1 || item.quantidade() > 999) {
                throw new RegraNegocioException("Quantidade inválida para " + produto.getNome() + ".");
            }
            itens.merge(produto.getId(),
                    new ItemProjeto(produto.getId(), item.quantidade(), item.essencial(), limitar(item.motivo(), 300)),
                    (atual, novo) -> new ItemProjeto(atual.idProduto(), atual.quantidade() + novo.quantidade(),
                            atual.essencial() || novo.essencial(), atual.motivo()));
        }
        String titulo = requisicao.titulo() == null || requisicao.titulo().isBlank()
                ? "Minha lista" : limitar(requisicao.titulo(), 120);
        long id = dao.inserir(titulo, limitar(requisicao.problema(), 1000), new ArrayList<>(itens.values()));
        return buscar(id);
    }

    public ProjetoDTO buscar(long id) {
        Projeto projeto = dao.buscar(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Projeto não encontrado."));
        return paraDTO(projeto);
    }

    public RotaDTO rota(long id) {
        Projeto projeto = dao.buscar(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Projeto não encontrado."));
        Map<Integer, List<String>> skusPorCorredor = new TreeMap<>();
        for (ItemProjeto item : projeto.itens()) {
            Produto produto = catalogo.obterPorId(item.idProduto());
            skusPorCorredor.computeIfAbsent(produto.getNumeroCorredor(), k -> new ArrayList<>()).add(produto.getSku());
        }
        return rotas.planejar(skusPorCorredor);
    }

    private ProjetoDTO paraDTO(Projeto projeto) {
        List<ItemSugeridoDTO> itens = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (ItemProjeto item : projeto.itens()) {
            Produto produto = catalogo.obterPorId(item.idProduto());
            BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(item.quantidade()));
            itens.add(new ItemSugeridoDTO(catalogo.paraDTO(produto), item.quantidade(), item.essencial(),
                    item.motivo(), subtotal));
            total = total.add(subtotal);
        }
        Comparator<ItemSugeridoDTO> porCorredor = Comparator.comparingInt(i -> i.produto().corredor());
        List<ItemSugeridoDTO> ordenados = Ordenacao.mergeSort(itens,
                porCorredor.thenComparing(i -> i.produto().prateleira()));
        int corredores = (int) ordenados.stream().map(i -> i.produto().corredor()).distinct().count();
        return new ProjetoDTO(projeto.id(), projeto.titulo(), projeto.problema(), projeto.criadoEm(), ordenados,
                total, ordenados.size(), corredores);
    }

    private static String limitar(String texto, int tamanho) {
        if (texto == null) {
            return null;
        }
        String limpo = texto.trim();
        return limpo.length() <= tamanho ? limpo : limpo.substring(0, tamanho);
    }
}
