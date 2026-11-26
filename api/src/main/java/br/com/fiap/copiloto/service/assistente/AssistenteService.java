package br.com.fiap.copiloto.service.assistente;

import br.com.fiap.copiloto.dto.AssistenteResposta;
import br.com.fiap.copiloto.dto.ComplementoDTO;
import br.com.fiap.copiloto.dto.ConversaRequest;
import br.com.fiap.copiloto.dto.ItemSugeridoDTO;
import br.com.fiap.copiloto.dto.MensagemDTO;
import br.com.fiap.copiloto.exception.RegraNegocioException;
import br.com.fiap.copiloto.model.Complemento;
import br.com.fiap.copiloto.model.Produto;
import br.com.fiap.copiloto.model.TipoAtendimento;
import br.com.fiap.copiloto.service.CatalogoService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AssistenteService {

    private static final int MAX_MENSAGENS = 20;
    private static final int MAX_COMPLEMENTOS = 4;
    private static final int MAX_IMAGEM_CHARS = 6_000_000;
    public static final int MAX_TEXTO_CLIENTE = 500;

    private final CatalogoService catalogo;
    private final OpenAiCliente ia;
    private final RegrasAssistente regras;

    public AssistenteService(CatalogoService catalogo) {
        this.catalogo = catalogo;
        this.ia = new OpenAiCliente(catalogo);
        this.regras = new RegrasAssistente(catalogo);
    }

    public boolean iaConfigurada() {
        return ia.configurado();
    }

    public AssistenteResposta responder(ConversaRequest requisicao) {
        if (requisicao == null || requisicao.mensagens() == null || requisicao.mensagens().isEmpty()) {
            throw new RegraNegocioException("Envie pelo menos uma mensagem.");
        }
        String imagem = requisicao.imagemBase64();
        if (imagem != null && imagem.length() > MAX_IMAGEM_CHARS) {
            throw new RegraNegocioException("A foto é muito grande. Tente uma imagem menor.");
        }
        List<MensagemDTO> mensagens = requisicao.mensagens();
        boolean textoLongo = mensagens.stream()
                .anyMatch(m -> "cliente".equals(m.papel()) && m.texto() != null && m.texto().length() > MAX_TEXTO_CLIENTE);
        if (textoLongo) {
            throw new RegraNegocioException("Sua mensagem ficou longa. Resuma em até " + MAX_TEXTO_CLIENTE + " caracteres.");
        }
        if (mensagens.size() > MAX_MENSAGENS) {
            mensagens = mensagens.subList(mensagens.size() - MAX_MENSAGENS, mensagens.size());
        }
        boolean temFoto = imagem != null && !imagem.isBlank();

        Sugestao sugestao = null;
        String fonte = "REGRAS";
        if (ia.configurado()) {
            try {
                sugestao = ia.conversar(mensagens, imagem);
                fonte = "IA";
            } catch (Exception e) {
                System.err.println("AI unavailable, using rules fallback: " + e.getMessage());
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        if (sugestao == null) {
            sugestao = regras.responder(mensagens, temFoto);
        }
        return enriquecer(sugestao, fonte);
    }

    private AssistenteResposta enriquecer(Sugestao sugestao, String fonte) {
        Map<String, ItemSugeridoDTO> itens = new LinkedHashMap<>();
        boolean especialista = sugestao.precisaEspecialista() && !sugestao.itens().isEmpty();
        for (Sugestao.Item item : sugestao.itens()) {
            Optional<Produto> encontrado = catalogo.buscarPorSku(item.sku());
            if (encontrado.isEmpty() || itens.containsKey(encontrado.get().getSku())) {
                continue;
            }
            Produto produto = encontrado.get();
            int quantidade = Math.max(1, Math.min(item.quantidade(), 999));
            if (produto.getTipoAtendimento() == TipoAtendimento.DEPENDE_VENDEDOR) {
                especialista = true;
            }
            itens.put(produto.getSku(), new ItemSugeridoDTO(catalogo.paraDTO(produto), quantidade, item.essencial(),
                    item.motivo(), produto.getPreco().multiply(BigDecimal.valueOf(quantidade))));
        }

        List<ComplementoDTO> complementos = new ArrayList<>();
        Set<String> jaSugeridos = new HashSet<>(itens.keySet());
        for (ItemSugeridoDTO item : itens.values()) {
            Produto produto = catalogo.obterPorSku(item.produto().sku());
            for (Complemento complemento : catalogo.complementosDe(produto)) {
                Produto extra = catalogo.obterPorId(complemento.idComplementar());
                if (complementos.size() < MAX_COMPLEMENTOS && jaSugeridos.add(extra.getSku())) {
                    complementos.add(new ComplementoDTO(catalogo.paraDTO(extra), complemento.motivo(), produto.getSku()));
                }
            }
        }

        return new AssistenteResposta(
                sugestao.resposta(),
                itens.isEmpty() ? null : tituloOuPadrao(sugestao.tituloProjeto()),
                sugestao.perguntasRapidas() == null ? List.of() : sugestao.perguntasRapidas(),
                sugestao.diagnostico() == null ? List.of() : sugestao.diagnostico(),
                new ArrayList<>(itens.values()),
                complementos,
                especialista,
                fonte);
    }

    private static String tituloOuPadrao(String titulo) {
        return titulo == null || titulo.isBlank() ? "Meu projeto" : titulo;
    }
}
