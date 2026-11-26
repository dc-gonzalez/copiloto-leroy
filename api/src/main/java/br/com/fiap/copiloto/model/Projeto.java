package br.com.fiap.copiloto.model;

import java.time.LocalDateTime;
import java.util.List;

public record Projeto(long id, String titulo, String problema, LocalDateTime criadoEm, List<ItemProjeto> itens) {
}
