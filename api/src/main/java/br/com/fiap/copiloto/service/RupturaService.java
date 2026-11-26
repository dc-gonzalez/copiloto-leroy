package br.com.fiap.copiloto.service;

import br.com.fiap.copiloto.dao.RupturaDAO;
import br.com.fiap.copiloto.dto.AlternativasDTO;
import br.com.fiap.copiloto.dto.ProdutoDTO;
import br.com.fiap.copiloto.dto.RupturaRequest;
import br.com.fiap.copiloto.exception.ProdutoIndisponivelException;
import br.com.fiap.copiloto.exception.RecursoNaoEncontradoException;
import br.com.fiap.copiloto.exception.RegraNegocioException;
import br.com.fiap.copiloto.model.DesfechoRuptura;
import br.com.fiap.copiloto.model.Produto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Handles "I couldn't find it on the shelf": records the gap and offers a way out. */
public class RupturaService {

    private final RupturaDAO dao;
    private final CatalogoService catalogo;

    public RupturaService(RupturaDAO dao, CatalogoService catalogo) {
        this.dao = dao;
        this.catalogo = catalogo;
    }

    public AlternativasDTO registrar(RupturaRequest requisicao) {
        if (requisicao == null || requisicao.sku() == null) {
            throw new RegraNegocioException("Informe o produto que não foi encontrado.");
        }
        Produto produto = catalogo.obterPorSku(requisicao.sku());
        catalogo.registrarGondolaVazia(produto);
        long id = dao.inserir(produto.getId(), requisicao.projetoId());
        return alternativas(id, produto);
    }

    /** Alternatives without recording anything (used when the catalog already knows the shelf is empty). */
    public AlternativasDTO alternativas(ProdutoIndisponivelException indisponivel) {
        return alternativas(0, indisponivel.getProduto());
    }

    public void registrarDesfecho(long idRuptura, String desfecho) {
        DesfechoRuptura valor;
        try {
            valor = DesfechoRuptura.valueOf(desfecho);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new RegraNegocioException("Desfecho inválido.");
        }
        if (!dao.atualizarDesfecho(idRuptura, valor)) {
            throw new RecursoNaoEncontradoException("Registro de ruptura não encontrado.");
        }
    }

    private AlternativasDTO alternativas(long idRuptura, Produto produto) {
        List<ProdutoDTO> similares = catalogo.alternativasDisponiveis(produto).stream()
                .map(catalogo::paraDTO)
                .toList();
        boolean temNoCentro = produto.getEstoqueSistema() > 0;
        AlternativasDTO.CompraOnlineDTO online = new AlternativasDTO.CompraOnlineDTO(
                "https://www.leroymerlin.com.br/busca?term=" + URLEncoder.encode(produto.getNome(), StandardCharsets.UTF_8),
                temNoCentro ? "Receba amanhã em casa ou retire aqui em 2 horas" : "Receba em até 3 dias úteis",
                true);

        String mensagem;
        if (!similares.isEmpty() && similares.get(0).marcaPropria() && !produto.isMarcaPropria()) {
            mensagem = "Esse acabou na prateleira, mas temos um equivalente da nossa marca, que custa menos.";
        } else if (!similares.isEmpty()) {
            mensagem = "Esse acabou na prateleira, mas temos uma opção equivalente aqui na loja.";
        } else {
            mensagem = "Esse acabou na prateleira. Você pode comprar online ou chamar um vendedor para checar o estoque.";
        }
        return new AlternativasDTO(idRuptura, catalogo.paraDTO(produto), mensagem, similares, online);
    }
}
