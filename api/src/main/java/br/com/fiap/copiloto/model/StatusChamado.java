package br.com.fiap.copiloto.model;

public enum StatusChamado {
    ABERTO("aberto"),
    A_CAMINHO("em atendimento"),
    CONCLUIDO("concluído"),
    CANCELADO("cancelado");

    private final String descricao;

    StatusChamado(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean podeAvancarPara(StatusChamado proximo) {
        return switch (this) {
            case ABERTO -> proximo == A_CAMINHO || proximo == CANCELADO;
            case A_CAMINHO -> proximo == CONCLUIDO || proximo == CANCELADO;
            case CONCLUIDO, CANCELADO -> false;
        };
    }
}
