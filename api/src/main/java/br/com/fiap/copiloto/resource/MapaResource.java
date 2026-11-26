package br.com.fiap.copiloto.resource;

import br.com.fiap.copiloto.dto.MapaDTO;
import br.com.fiap.copiloto.service.Servicos;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/mapa")
@Produces(MediaType.APPLICATION_JSON)
public class MapaResource {

    @GET
    public MapaDTO mapa() {
        return Servicos.get().rotas().mapa();
    }
}
