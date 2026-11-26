package br.com.fiap.copiloto.dto;

import java.util.List;

public record RotaDTO(
        List<ParadaDTO> paradas,
        List<PontoDTO> caminho,
        double distanciaTotalM,
        int tempoEstimadoMin,
        double distanciaSemOtimizarM) {
}
