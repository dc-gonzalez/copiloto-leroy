package br.com.fiap.copiloto.dto;

import java.util.List;

public record AlternativasDTO(
        long rupturaId,
        ProdutoDTO produto,
        String mensagem,
        List<ProdutoDTO> similares,
        CompraOnlineDTO compraOnline) {

    public record CompraOnlineDTO(String url, String prazo, boolean disponivel) {
    }
}
