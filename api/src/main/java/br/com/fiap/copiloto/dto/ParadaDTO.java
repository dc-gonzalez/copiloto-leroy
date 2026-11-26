package br.com.fiap.copiloto.dto;

import java.util.List;

public record ParadaDTO(
        int ordem,
        int corredor,
        String secao,
        String cor,
        int x,
        int y,
        List<String> skus,
        double distanciaDesdeAnteriorM) {
}
