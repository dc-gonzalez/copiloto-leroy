package br.com.fiap.copiloto.resource;

import br.com.fiap.copiloto.dto.ProjetoDTO;
import br.com.fiap.copiloto.dto.ProjetoRequest;
import br.com.fiap.copiloto.dto.RotaDTO;
import br.com.fiap.copiloto.service.Servicos;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/projetos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProjetoResource {

    @POST
    public Response criar(ProjetoRequest requisicao, @Context UriInfo uri) {
        ProjetoDTO projeto = Servicos.get().projetos().criar(requisicao);
        return Response.created(uri.getAbsolutePathBuilder().path(String.valueOf(projeto.id())).build())
                .entity(projeto)
                .build();
    }

    @GET
    @Path("/{id}")
    public ProjetoDTO buscar(@PathParam("id") long id) {
        return Servicos.get().projetos().buscar(id);
    }

    @GET
    @Path("/{id}/rota")
    public RotaDTO rota(@PathParam("id") long id) {
        return Servicos.get().projetos().rota(id);
    }
}
