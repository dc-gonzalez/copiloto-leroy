package br.com.fiap.copiloto.dto;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;

/** papel is "cliente" or "assistente". */
public record MensagemDTO(@JsonbProperty("papel") String papel, @JsonbProperty("texto") String texto) {

    @JsonbCreator
    public MensagemDTO {
    }
}
