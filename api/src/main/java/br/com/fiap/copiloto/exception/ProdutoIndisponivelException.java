package br.com.fiap.copiloto.exception;

import br.com.fiap.copiloto.model.Produto;

/** Thrown when the system says there is stock but the shelf is empty ("ruptura"). */
public class ProdutoIndisponivelException extends Exception {

    private final transient Produto produto;

    public ProdutoIndisponivelException(Produto produto) {
        super("Produto " + produto.getSku() + " indisponível na gôndola");
        this.produto = produto;
    }

    public Produto getProduto() {
        return produto;
    }
}
