package br.com.fiap.copiloto.dto;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;

public record DesfechoRequest(@JsonbProperty("desfecho") String desfecho) {

    @JsonbCreator
    public DesfechoRequest {
    }
}
