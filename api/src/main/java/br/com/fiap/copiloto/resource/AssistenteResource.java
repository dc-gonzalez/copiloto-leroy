package br.com.fiap.copiloto.resource;

import br.com.fiap.copiloto.dto.AssistenteResposta;
import br.com.fiap.copiloto.dto.ConversaRequest;
import br.com.fiap.copiloto.service.Servicos;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/assistente")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AssistenteResource {

    @POST
    @Path("/conversa")
    public AssistenteResposta conversar(ConversaRequest requisicao) {
        return Servicos.get().assistente().responder(requisicao);
    }
}
