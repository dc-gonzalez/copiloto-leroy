package br.com.fiap.copiloto.service;

import br.com.fiap.copiloto.dao.ChamadoDAO;
import br.com.fiap.copiloto.dto.ChamadoDTO;
import br.com.fiap.copiloto.dto.ChamadoRequest;
import br.com.fiap.copiloto.dto.ProdutoDTO;
import br.com.fiap.copiloto.dto.ProjetoDTO;
import br.com.fiap.copiloto.exception.RecursoNaoEncontradoException;
import br.com.fiap.copiloto.exception.RegraNegocioException;
import br.com.fiap.copiloto.model.Chamado;
import br.com.fiap.copiloto.model.Corredor;
import br.com.fiap.copiloto.model.Produto;
import br.com.fiap.copiloto.model.StatusChamado;

import java.util.ArrayList;
import java.util.List;

/** Help requests from customers, served to associates in arrival order (FIFO). */
public class ChamadoService {

    private final ChamadoDAO dao;
    private final CatalogoService catalogo;
    private final ProjetoService projetos;

    public ChamadoService(ChamadoDAO dao, CatalogoService catalogo, ProjetoService projetos) {
        this.dao = dao;
        this.catalogo = catalogo;
        this.projetos = projetos;
    }

    public ChamadoDTO abrir(ChamadoRequest requisicao) {
        if (requisicao == null) {
            throw new RegraNegocioException("Dados do chamado não informados.");
        }
        Produto produto = requisicao.sku() == null || requisicao.sku().isBlank()
                ? null : catalogo.obterPorSku(requisicao.sku());
        ProjetoDTO projeto = requisicao.projetoId() == null ? null : projetos.buscar(requisicao.projetoId());

        Integer numeroCorredor = requisicao.corredor();
        if (numeroCorredor == null && produto != null) {
            numeroCorredor = produto.getNumeroCorredor();
        }
        if (numeroCorredor == null && projeto != null && !projeto.itens().isEmpty()) {
            numeroCorredor = projeto.itens().get(0).produto().corredor();
        }
        if (numeroCorredor == null) {
            throw new RegraNegocioException("Informe em qual corredor você está.");
        }
        Corredor corredor = catalogo.corredor(numeroCorredor);

        String motivo = requisicao.motivo() == null || requisicao.motivo().isBlank()
                ? "Cliente pediu ajuda de um especialista." : requisicao.motivo().trim();
        if (motivo.length() > 500) {
            motivo = motivo.substring(0, 500);
        }
        long id = dao.inserir(projeto == null ? null : projeto.id(), corredor.id(),
                produto == null ? null : produto.getId(), motivo);
        return buscar(id);
    }

    public ChamadoDTO buscar(long id) {
        Chamado chamado = dao.buscar(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado não encontrado."));
        int posicao = chamado.status() == StatusChamado.ABERTO ? dao.contarNaFrente(id) + 1 : 0;
        return paraDTO(chamado, posicao, true);
    }

    public List<ChamadoDTO> fila() {
        List<ChamadoDTO> fila = new ArrayList<>();
        int posicao = 0;
        for (Chamado chamado : dao.listarFila()) {
            int posicaoAtual = chamado.status() == StatusChamado.ABERTO ? ++posicao : 0;
            fila.add(paraDTO(chamado, posicaoAtual, true));
        }
        return fila;
    }

    public ChamadoDTO aceitar(long id, String vendedor) {
        Chamado chamado = validarTransicao(id, StatusChamado.A_CAMINHO);
        String nome = vendedor == null || vendedor.isBlank() ? "Especialista" : vendedor.trim();
        dao.aceitar(chamado.id(), nome.length() > 60 ? nome.substring(0, 60) : nome);
        return buscar(id);
    }

    public ChamadoDTO concluir(long id) {
        validarTransicao(id, StatusChamado.CONCLUIDO);
        dao.encerrar(id, StatusChamado.CONCLUIDO);
        return buscar(id);
    }

    public ChamadoDTO cancelar(long id) {
        validarTransicao(id, StatusChamado.CANCELADO);
        dao.encerrar(id, StatusChamado.CANCELADO);
        return buscar(id);
    }

    private Chamado validarTransicao(long id, StatusChamado proximo) {
        Chamado chamado = dao.buscar(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado não encontrado."));
        if (!chamado.status().podeAvancarPara(proximo)) {
            throw new RegraNegocioException("Este chamado já está " + chamado.status().getDescricao() + ".");
        }
        return chamado;
    }

    private ChamadoDTO paraDTO(Chamado chamado, int posicao, boolean incluirProjeto) {
        Corredor corredor = catalogo.corredorPorId(chamado.idCorredor());
        ProdutoDTO produto = chamado.idProduto() == null ? null : catalogo.paraDTO(catalogo.obterPorId(chamado.idProduto()));
        ProjetoDTO projeto = null;
        if (incluirProjeto && chamado.idProjeto() != null) {
            try {
                projeto = projetos.buscar(chamado.idProjeto());
            } catch (RecursoNaoEncontradoException ignorado) {
                projeto = null;
            }
        }
        return new ChamadoDTO(chamado.id(), chamado.senha(), chamado.status().name(), corredor.numero(),
                corredor.secao().nome(), corredor.secao().cor(), chamado.motivo(), chamado.vendedor(),
                chamado.abertoEm(), chamado.aceitoEm(), chamado.concluidoEm(),
                chamado.minutosDeEspera(), posicao, produto, projeto);
    }
}
