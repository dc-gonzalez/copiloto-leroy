package br.com.fiap.copiloto.infra;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.util.Arrays;

@Provider
@PreMatching
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            request.abortWith(Response.ok().build());
        }
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        String origem = request.getHeaderString("Origin");
        if (origem == null) {
            return;
        }
        String permitidas = Config.origensPermitidas();
        boolean liberada = "*".equals(permitidas)
                || Arrays.stream(permitidas.split(",")).map(String::trim).anyMatch(origem::equals);
        if (!liberada) {
            return;
        }
        response.getHeaders().putSingle("Access-Control-Allow-Origin", origem);
        response.getHeaders().putSingle("Vary", "Origin");
        response.getHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.getHeaders().putSingle("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.getHeaders().putSingle("Access-Control-Max-Age", "86400");
    }
}
