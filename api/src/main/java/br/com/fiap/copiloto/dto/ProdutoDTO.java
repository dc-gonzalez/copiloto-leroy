package br.com.fiap.copiloto.dto;

import java.math.BigDecimal;

public record ProdutoDTO(
        String sku,
        String nome,
        String descricao,
        String marca,
        boolean marcaPropria,
        BigDecimal preco,
        String unidade,
        int corredor,
        String secao,
        String corSecao,
        int prateleira,
        String tipoAtendimento,
        String atendimentoDescricao,
        String disponibilidade,
        boolean podeLevarAgora,
        boolean emRuptura,
        int estoqueGondola,
        int estoqueSistema,
        String orientacao) {
}
