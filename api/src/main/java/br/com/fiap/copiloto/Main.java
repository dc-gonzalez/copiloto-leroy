package br.com.fiap.copiloto;

import br.com.fiap.copiloto.infra.Config;
import br.com.fiap.copiloto.infra.ConnectionFactory;
import br.com.fiap.copiloto.service.Servicos;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.util.concurrent.CountDownLatch;

public class Main {

    public static HttpServer startServer() {
        String baseUri = "http://0.0.0.0:" + Config.porta() + "/";
        ResourceConfig rc = new ResourceConfig().packages("br.com.fiap.copiloto");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(baseUri), rc);
    }

    public static void main(String[] args) throws Exception {
        Servicos.inicializar();
        HttpServer server = startServer();
        System.out.printf("Copiloto Leroy API listening on port %d%n", Config.porta());

        CountDownLatch encerrar = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdownNow();
            ConnectionFactory.fechar();
            encerrar.countDown();
        }));
        encerrar.await();
    }
}
