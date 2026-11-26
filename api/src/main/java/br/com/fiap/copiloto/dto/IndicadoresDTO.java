package br.com.fiap.copiloto.dto;

import java.math.BigDecimal;
import java.util.List;

public record IndicadoresDTO(
        int projetosHoje,
        BigDecimal ticketMedioProjeto,
        int chamadosAbertos,
        double minutosMedioAtendimento,
        int rupturasHoje,
        List<RupturaPorProduto> rupturasPorProduto,
        List<ChamadosPorSecao> chamadosPorSecao,
        List<ProdutoMaisPedido> produtosMaisPedidos) {

    public record RupturaPorProduto(String sku, String nome, int corredor, int ocorrencias, String ultimoDesfecho,
                                    boolean prateleiraVazia, int estoqueSistema) {
    }

    public record ChamadosPorSecao(String secao, String cor, int total) {
    }

    public record ProdutoMaisPedido(String sku, String nome, int projetos, int unidades) {
    }
}
