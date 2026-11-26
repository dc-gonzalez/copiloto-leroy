package br.com.fiap.copiloto.dto;

import java.time.LocalDateTime;

public record ChamadoDTO(
        long id,
        int senha,
        String status,
        int corredor,
        String secao,
        String corSecao,
        String motivo,
        String vendedor,
        LocalDateTime abertoEm,
        LocalDateTime aceitoEm,
        LocalDateTime concluidoEm,
        long minutosEsperando,
        int posicaoNaFila,
        ProdutoDTO produto,
        ProjetoDTO projeto) {
}
