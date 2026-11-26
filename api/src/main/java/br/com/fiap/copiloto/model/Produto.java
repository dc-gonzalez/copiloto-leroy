package br.com.fiap.copiloto.model;

import java.math.BigDecimal;

/**
 * Product supertype. Subclasses decide how the customer gets the item
 * (take it now vs. see the display and receive it at home).
 */
public abstract class Produto {

    private final long id;
    private final String sku;
    private final String nome;
    private final String descricao;
    private final String marca;
    private final boolean marcaPropria;
    private final BigDecimal preco;
    private final String unidade;
    private final int numeroCorredor;
    private final int prateleira;
    private final TipoAtendimento tipoAtendimento;
    private final String palavrasChave;
    private int estoqueSistema;
    private int estoqueGondola;

    protected Produto(long id, String sku, String nome, String descricao, String marca, boolean marcaPropria,
                      BigDecimal preco, String unidade, int numeroCorredor, int prateleira,
                      TipoAtendimento tipoAtendimento, String palavrasChave, int estoqueSistema, int estoqueGondola) {
        this.id = id;
        this.sku = sku;
        this.nome = nome;
        this.descricao = descricao;
        this.marca = marca;
        this.marcaPropria = marcaPropria;
        this.preco = preco;
        this.unidade = unidade;
        this.numeroCorredor = numeroCorredor;
        this.prateleira = prateleira;
        this.tipoAtendimento = tipoAtendimento;
        this.palavrasChave = palavrasChave == null ? "" : palavrasChave;
        this.estoqueSistema = estoqueSistema;
        this.estoqueGondola = estoqueGondola;
    }

    public abstract TipoDisponibilidade getDisponibilidade();

    /** True when the customer can pick it from the shelf right now. */
    public abstract boolean podeLevarAgora();

    /** Short sentence shown to the customer explaining how they get the item. */
    public abstract String orientacaoCompra();

    public boolean estaEmRuptura() {
        return getDisponibilidade() == TipoDisponibilidade.PRONTA_ENTREGA && estoqueGondola == 0;
    }

    public void registrarGondolaVazia() {
        this.estoqueGondola = 0;
    }

    public void reporGondola(int quantidade) {
        if (quantidade < 0 || quantidade > estoqueSistema) {
            throw new IllegalArgumentException("Shelf quantity out of range: " + quantidade);
        }
        this.estoqueGondola = quantidade;
    }

    public long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getMarca() {
        return marca;
    }

    public boolean isMarcaPropria() {
        return marcaPropria;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public String getUnidade() {
        return unidade;
    }

    public int getNumeroCorredor() {
        return numeroCorredor;
    }

    public int getPrateleira() {
        return prateleira;
    }

    public TipoAtendimento getTipoAtendimento() {
        return tipoAtendimento;
    }

    public String getPalavrasChave() {
        return palavrasChave;
    }

    public int getEstoqueSistema() {
        return estoqueSistema;
    }

    public int getEstoqueGondola() {
        return estoqueGondola;
    }
}
