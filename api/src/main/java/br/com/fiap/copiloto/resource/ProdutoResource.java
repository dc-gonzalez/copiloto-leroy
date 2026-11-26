package br.com.fiap.copiloto.resource;

import br.com.fiap.copiloto.dto.LocalizacaoDTO;
import br.com.fiap.copiloto.dto.ProdutoDTO;
import br.com.fiap.copiloto.exception.ProdutoIndisponivelException;
import br.com.fiap.copiloto.model.Produto;
import br.com.fiap.copiloto.service.CatalogoService;
import br.com.fiap.copiloto.service.Servicos;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@Path("/produtos")
@Produces(MediaType.APPLICATION_JSON)
public class ProdutoResource {

    @GET
    public List<ProdutoDTO> pesquisar(@QueryParam("busca") String busca,
                                      @QueryParam("limite") @DefaultValue("12") int limite) {
        CatalogoService catalogo = Servicos.get().catalogo();
        int maximo = Math.max(1, Math.min(limite, 50));
        List<Produto> produtos = busca == null || busca.isBlank()
                ? catalogo.todos().stream().limit(maximo).toList()
                : catalogo.pesquisar(busca, maximo);
        return produtos.stream().map(catalogo::paraDTO).toList();
    }

    @GET
    @Path("/{sku}")
    public ProdutoDTO buscar(@PathParam("sku") String sku) {
        CatalogoService catalogo = Servicos.get().catalogo();
        return catalogo.paraDTO(catalogo.obterPorSku(sku));
    }

    /** Associate confirms the shelf was restocked from the back room. */
    @PUT
    @Path("/{sku}/reposicao")
    public ProdutoDTO repor(@PathParam("sku") String sku) {
        CatalogoService catalogo = Servicos.get().catalogo();
        Produto produto = catalogo.obterPorSku(sku);
        catalogo.reporGondola(produto);
        return catalogo.paraDTO(produto);
    }

    /** Route to a single product. 409 with alternatives when the shelf is known to be empty. */
    @GET
    @Path("/{sku}/localizacao")
    public Response localizar(@PathParam("sku") String sku) {
        Servicos servicos = Servicos.get();
        CatalogoService catalogo = servicos.catalogo();
        Produto produto = catalogo.obterPorSku(sku);
        try {
            catalogo.garantirNaGondola(produto);
        } catch (ProdutoIndisponivelException e) {
            return Response.status(Response.Status.CONFLICT).entity(servicos.rupturas().alternativas(e)).build();
        }
        var rota = servicos.rotas().planejar(Map.of(produto.getNumeroCorredor(), List.of(produto.getSku())));
        return Response.ok(new LocalizacaoDTO(catalogo.paraDTO(produto), rota)).build();
    }
}
