package br.com.fiap.copiloto.dto;

import java.util.List;

public record MapaDTO(List<NoDTO> nos, List<ArestaDTO> arestas, List<CorredorDTO> corredores) {

    public record NoDTO(long id, String codigo, String tipo, int x, int y) {
    }

    public record ArestaDTO(long origem, long destino, double metros) {
    }

    public record CorredorDTO(int numero, String secao, String cor, int x, int y) {
    }
}
