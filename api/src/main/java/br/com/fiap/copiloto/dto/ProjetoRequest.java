package br.com.fiap.copiloto.dto;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;

import java.util.List;

public record ProjetoRequest(@JsonbProperty("titulo") String titulo,
                             @JsonbProperty("problema") String problema,
                             @JsonbProperty("itens") List<ItemRequest> itens) {

    @JsonbCreator
    public ProjetoRequest {
    }
}
