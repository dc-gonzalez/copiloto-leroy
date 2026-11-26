package br.com.fiap.copiloto.service.assistente;

import java.util.List;

/** Raw answer from the AI or the rules engine, before SKUs are validated and priced. */
public record Sugestao(
        String resposta,
        String tituloProjeto,
        List<String> perguntasRapidas,
        List<String> diagnostico,
        List<Item> itens,
        boolean precisaEspecialista) {

    public record Item(String sku, int quantidade, boolean essencial, String motivo) {
    }
}
