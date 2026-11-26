package br.com.fiap.copiloto.model;

/** How much an associate is needed to buy the product (classification given by Leroy at the kickoff). */
public enum TipoAtendimento {
    AUTONOMO("Você consegue sozinho"),
    APOIO_PARCIAL("Pode precisar de uma dica"),
    DEPENDE_VENDEDOR("Precisa de um especialista");

    private final String descricao;

    TipoAtendimento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
