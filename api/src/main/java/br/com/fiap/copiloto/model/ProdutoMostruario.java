package br.com.fiap.copiloto.model;

import java.math.BigDecimal;

/** Display-only product (e.g. porcelain tiles): seen in the store, delivered from the distribution center. */
public class ProdutoMostruario extends Produto {

    public ProdutoMostruario(long id, String sku, String nome, String descricao, String marca, boolean marcaPropria,
                             BigDecimal preco, String unidade, int numeroCorredor, int prateleira,
                             TipoAtendimento tipoAtendimento, String palavrasChave,
                             int estoqueSistema, int estoqueGondola) {
        super(id, sku, nome, descricao, marca, marcaPropria, preco, unidade, numeroCorredor, prateleira,
                tipoAtendimento, palavrasChave, estoqueSistema, estoqueGondola);
    }

    @Override
    public TipoDisponibilidade getDisponibilidade() {
        return TipoDisponibilidade.MOSTRUARIO;
    }

    @Override
    public boolean podeLevarAgora() {
        return false;
    }

    @Override
    public String orientacaoCompra() {
        return "Veja e toque a peça no mostruário do corredor " + getNumeroCorredor()
                + ". Você paga no caixa e recebe em casa.";
    }
}
