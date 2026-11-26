package br.com.fiap.copiloto.resource;

import br.com.fiap.copiloto.dto.IndicadoresDTO;
import br.com.fiap.copiloto.service.Servicos;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/indicadores")
@Produces(MediaType.APPLICATION_JSON)
public class IndicadorResource {

    @GET
    public IndicadoresDTO carregar() {
        return Servicos.get().indicadores().carregar();
    }
}
