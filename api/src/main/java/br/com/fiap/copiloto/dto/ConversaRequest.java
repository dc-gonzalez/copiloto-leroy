package br.com.fiap.copiloto.dto;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;

import java.util.List;

/** imagemBase64 is an optional data URL (data:image/jpeg;base64,...) of the customer's photo. */
public record ConversaRequest(@JsonbProperty("mensagens") List<MensagemDTO> mensagens,
                              @JsonbProperty("imagemBase64") String imagemBase64) {

    @JsonbCreator
    public ConversaRequest {
    }
}
