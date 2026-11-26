package br.com.fiap.copiloto.dto;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;

public record ChamadoRequest(@JsonbProperty("projetoId") Long projetoId,
                             @JsonbProperty("corredor") Integer corredor,
                             @JsonbProperty("sku") String sku,
                             @JsonbProperty("motivo") String motivo) {

    @JsonbCreator
    public ChamadoRequest {
    }
}
