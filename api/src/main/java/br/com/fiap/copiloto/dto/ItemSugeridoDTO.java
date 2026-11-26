package br.com.fiap.copiloto.dto;

import java.math.BigDecimal;

public record ItemSugeridoDTO(ProdutoDTO produto, int quantidade, boolean essencial, String motivo, BigDecimal subtotal) {
}
