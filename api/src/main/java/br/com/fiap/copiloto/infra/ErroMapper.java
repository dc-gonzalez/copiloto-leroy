package br.com.fiap.copiloto.infra;

import br.com.fiap.copiloto.dto.ErroDTO;
import br.com.fiap.copiloto.exception.DadosIndisponiveisException;
import br.com.fiap.copiloto.exception.RecursoNaoEncontradoException;
import br.com.fiap.copiloto.exception.RegraNegocioException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ErroMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable erro) {
        int status;
        String mensagem;
        if (erro instanceof RecursoNaoEncontradoException) {
            status = 404;
            mensagem = erro.getMessage();
        } else if (erro instanceof RegraNegocioException) {
            status = 422;
            mensagem = erro.getMessage();
        } else if (erro instanceof DadosIndisponiveisException) {
            status = 503;
            mensagem = "Não conseguimos acessar os dados da loja agora. Tente novamente em instantes.";
            erro.printStackTrace();
        } else if (erro instanceof WebApplicationException web) {
            status = web.getResponse().getStatus();
            mensagem = status == 404 ? "Endereço não encontrado." : "Requisição inválida.";
        } else {
            status = 500;
            mensagem = "Algo deu errado do nosso lado. Tente novamente.";
            erro.printStackTrace();
        }
        return Response.status(status)
                .entity(new ErroDTO(status, mensagem))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
