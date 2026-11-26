package br.com.fiap.copiloto.resource;

import br.com.fiap.copiloto.infra.Config;
import br.com.fiap.copiloto.service.Servicos;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.LinkedHashMap;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class SaudeResource {

    @GET
    public Map<String, Object> status() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("servico", "Copiloto Leroy API");
        boolean banco;
        try {
            banco = Servicos.get() != null;
        } catch (RuntimeException e) {
            banco = false;
        }
        status.put("banco", banco);
        status.put("ia", !Config.openAiChave().isBlank());
        status.put("produtos", banco ? Servicos.get().catalogo().todos().size() : 0);
        return status;
    }
}
