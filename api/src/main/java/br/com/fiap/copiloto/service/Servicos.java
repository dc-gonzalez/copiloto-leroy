package br.com.fiap.copiloto.service;

import br.com.fiap.copiloto.algoritmo.GrafoLoja;
import br.com.fiap.copiloto.dao.CatalogoDAO;
import br.com.fiap.copiloto.dao.ChamadoDAO;
import br.com.fiap.copiloto.dao.IndicadorDAO;
import br.com.fiap.copiloto.dao.MapaDAO;
import br.com.fiap.copiloto.dao.ProjetoDAO;
import br.com.fiap.copiloto.dao.RupturaDAO;
import br.com.fiap.copiloto.service.assistente.AssistenteService;

/**
 * Wires services once. Loading is lazy and retried on the next request if the
 * database was unreachable at startup, so the HTTP server can still come up.
 */
public final class Servicos {

    private static volatile Servicos instancia;

    private final CatalogoService catalogo;
    private final RotaService rotas;
    private final ProjetoService projetos;
    private final ChamadoService chamados;
    private final RupturaService rupturas;
    private final AssistenteService assistente;
    private final IndicadorDAO indicadores;

    private Servicos() {
        MapaDAO mapaDAO = new MapaDAO();
        this.catalogo = new CatalogoService(new CatalogoDAO());
        this.rotas = new RotaService(new GrafoLoja(mapaDAO.listarNos(), mapaDAO.listarArestas()), catalogo);
        this.projetos = new ProjetoService(new ProjetoDAO(), catalogo, rotas);
        this.chamados = new ChamadoService(new ChamadoDAO(), catalogo, projetos);
        this.rupturas = new RupturaService(new RupturaDAO(), catalogo);
        this.assistente = new AssistenteService(catalogo);
        this.indicadores = new IndicadorDAO();
    }

    public static void inicializar() {
        try {
            get();
            System.out.printf("Catalog loaded: %d products%n", get().catalogo.todos().size());
        } catch (RuntimeException e) {
            System.err.println("Database not reachable at startup, will retry on first request: " + e.getMessage());
        }
    }

    public static Servicos get() {
        Servicos atual = instancia;
        if (atual == null) {
            synchronized (Servicos.class) {
                atual = instancia;
                if (atual == null) {
                    atual = new Servicos();
                    instancia = atual;
                }
            }
        }
        return atual;
    }

    public static boolean carregado() {
        return instancia != null;
    }

    public CatalogoService catalogo() {
        return catalogo;
    }

    public RotaService rotas() {
        return rotas;
    }

    public ProjetoService projetos() {
        return projetos;
    }

    public ChamadoService chamados() {
        return chamados;
    }

    public RupturaService rupturas() {
        return rupturas;
    }

    public AssistenteService assistente() {
        return assistente;
    }

    public IndicadorDAO indicadores() {
        return indicadores;
    }
}
