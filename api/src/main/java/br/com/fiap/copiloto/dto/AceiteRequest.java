package br.com.fiap.copiloto.dto;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;

public record AceiteRequest(@JsonbProperty("vendedor") String vendedor) {

    @JsonbCreator
    public AceiteRequest {
    }
}
