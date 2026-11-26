package br.com.fiap.copiloto.resource;

import br.com.fiap.copiloto.dto.AlternativasDTO;
import br.com.fiap.copiloto.dto.DesfechoRequest;
import br.com.fiap.copiloto.dto.RupturaRequest;
import br.com.fiap.copiloto.service.Servicos;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/rupturas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RupturaResource {

    @POST
    public Response registrar(RupturaRequest requisicao) {
        AlternativasDTO alternativas = Servicos.get().rupturas().registrar(requisicao);
        return Response.status(Response.Status.CREATED).entity(alternativas).build();
    }

    @PUT
    @Path("/{id}/desfecho")
    public Response desfecho(@PathParam("id") long id, DesfechoRequest requisicao) {
        Servicos.get().rupturas().registrarDesfecho(id, requisicao == null ? null : requisicao.desfecho());
        return Response.noContent().build();
    }
}
