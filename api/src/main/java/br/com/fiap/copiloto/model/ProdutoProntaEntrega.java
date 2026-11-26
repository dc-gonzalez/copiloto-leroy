package br.com.fiap.copiloto.model;

import java.math.BigDecimal;

public class ProdutoProntaEntrega extends Produto {

    public ProdutoProntaEntrega(long id, String sku, String nome, String descricao, String marca, boolean marcaPropria,
                                BigDecimal preco, String unidade, int numeroCorredor, int prateleira,
                                TipoAtendimento tipoAtendimento, String palavrasChave,
                                int estoqueSistema, int estoqueGondola) {
        super(id, sku, nome, descricao, marca, marcaPropria, preco, unidade, numeroCorredor, prateleira,
                tipoAtendimento, palavrasChave, estoqueSistema, estoqueGondola);
    }

    @Override
    public TipoDisponibilidade getDisponibilidade() {
        return TipoDisponibilidade.PRONTA_ENTREGA;
    }

    @Override
    public boolean podeLevarAgora() {
        return getEstoqueGondola() > 0;
    }

    @Override
    public String orientacaoCompra() {
        if (getEstoqueGondola() > 0) {
            return "Pegue na prateleira " + getPrateleira() + " do corredor " + getNumeroCorredor() + ".";
        }
        if (getEstoqueSistema() > 0) {
            return "Pode estar no estoque da loja: peça a um vendedor ou compre online.";
        }
        return "Sem estoque nesta loja no momento.";
    }
}
