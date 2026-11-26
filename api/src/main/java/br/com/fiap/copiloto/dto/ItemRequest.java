package br.com.fiap.copiloto.dto;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;

public record ItemRequest(@JsonbProperty("sku") String sku,
                          @JsonbProperty("quantidade") int quantidade,
                          @JsonbProperty("essencial") boolean essencial,
                          @JsonbProperty("motivo") String motivo) {

    @JsonbCreator
    public ItemRequest {
    }
}
