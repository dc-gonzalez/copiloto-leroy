package br.com.fiap.copiloto.dto;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;

public record RupturaRequest(@JsonbProperty("sku") String sku, @JsonbProperty("projetoId") Long projetoId) {

    @JsonbCreator
    public RupturaRequest {
    }
}
