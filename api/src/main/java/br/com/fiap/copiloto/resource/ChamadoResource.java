package br.com.fiap.copiloto.resource;

import br.com.fiap.copiloto.dto.AceiteRequest;
import br.com.fiap.copiloto.dto.ChamadoDTO;
import br.com.fiap.copiloto.dto.ChamadoRequest;
import br.com.fiap.copiloto.service.Servicos;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;

@Path("/chamados")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChamadoResource {

    @POST
    public Response abrir(ChamadoRequest requisicao, @Context UriInfo uri) {
        ChamadoDTO chamado = Servicos.get().chamados().abrir(requisicao);
        return Response.created(uri.getAbsolutePathBuilder().path(String.valueOf(chamado.id())).build())
                .entity(chamado)
                .build();
    }

    @GET
    public List<ChamadoDTO> fila() {
        return Servicos.get().chamados().fila();
    }

    @GET
    @Path("/{id}")
    public ChamadoDTO buscar(@PathParam("id") long id) {
        return Servicos.get().chamados().buscar(id);
    }

    @PUT
    @Path("/{id}/aceite")
    public ChamadoDTO aceitar(@PathParam("id") long id, AceiteRequest requisicao) {
        return Servicos.get().chamados().aceitar(id, requisicao == null ? null : requisicao.vendedor());
    }

    @PUT
    @Path("/{id}/conclusao")
    public ChamadoDTO concluir(@PathParam("id") long id) {
        return Servicos.get().chamados().concluir(id);
    }

    @PUT
    @Path("/{id}/cancelamento")
    public ChamadoDTO cancelar(@PathParam("id") long id) {
        return Servicos.get().chamados().cancelar(id);
    }
}
