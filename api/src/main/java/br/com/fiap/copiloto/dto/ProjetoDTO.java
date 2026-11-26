package br.com.fiap.copiloto.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProjetoDTO(
        long id,
        String titulo,
        String problema,
        LocalDateTime criadoEm,
        List<ItemSugeridoDTO> itens,
        BigDecimal total,
        int totalItens,
        int totalCorredores) {
}
