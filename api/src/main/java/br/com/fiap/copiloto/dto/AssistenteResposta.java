package br.com.fiap.copiloto.dto;

import java.util.List;

/** fonte is "IA" when answered by the language model, "REGRAS" when the offline fallback answered. */
public record AssistenteResposta(
        String resposta,
        String tituloProjeto,
        List<String> perguntasRapidas,
        List<String> diagnostico,
        List<ItemSugeridoDTO> itens,
        List<ComplementoDTO> complementos,
        boolean precisaEspecialista,
        String fonte) {
}
