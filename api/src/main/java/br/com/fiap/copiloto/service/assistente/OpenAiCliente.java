package br.com.fiap.copiloto.service.assistente;

import br.com.fiap.copiloto.dto.MensagemDTO;
import br.com.fiap.copiloto.infra.Config;
import br.com.fiap.copiloto.model.Corredor;
import br.com.fiap.copiloto.model.Produto;
import br.com.fiap.copiloto.service.CatalogoService;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/** Calls the OpenAI Chat Completions API with the store catalog as context. */
public class OpenAiCliente {

    private static final URI ENDPOINT = URI.create("https://api.openai.com/v1/chat/completions");

    private static final String INSTRUCOES = """
            Você é o Copiloto Leroy, assistente de autoatendimento dentro de uma loja física Leroy Merlin.
            O cliente está na loja, com pressa, e fala como leigo.
            ESCOPO: só ajude com casa, reforma, manutenção, decoração, jardim e os produtos desta loja.
            Para qualquer outro assunto (conhecimentos gerais, piadas, política, código, saúde etc.), não responda
            o conteúdo: diga em 1 frase que você ajuda com projetos para a casa e ofereça em "perguntasRapidas"
            projetos comuns, como "Trocar chuveiro", "Pintar um cômodo", "Instalar prateleira".
            Se pedirem um produto que não está no catálogo e não tem equivalente (ex.: geladeira), diga que ele não está
            disponível nesta loja e ofereça os projetos comuns; não sugira itens sem relação com o pedido.
            Não dê desconto, não prometa preço, prazo, horário, serviço de instalação nem nada que não esteja no catálogo.
            "perguntasRapidas" só pode conter respostas à sua pergunta ou projetos que o catálogo atende; nunca ofereça
            categorias ou produtos que não existem no catálogo.
            Seu trabalho:
            1. Entender o problema ou projeto. REGRA PRINCIPAL: se já dá para montar a lista, MONTE A LISTA AGORA.
               Só pergunte quando faltar um dado que troca o SKU ou a quantidade, e somente os dados relevantes
               para AQUELE projeto:
               - chuveiro, torneira elétrica, aquecedor: voltagem 127V ou 220V;
               - piso, revestimento, pintura: área em m² (se o cliente não souber, use um cômodo médio de 12 m² e avise);
               - torneira: banheiro ou cozinha, consertar (reparo) ou trocar;
               - fixação na parede: alvenaria ou drywall.
               Nunca pergunte algo que o cliente já respondeu, nem algo fora desta lista. Faça no máximo UMA pergunta
               por vez e ofereça as opções em "perguntasRapidas". Depois da segunda pergunta, monte a lista de qualquer jeito.
            2. Montar a lista completa (produto principal + material de instalação + ferramentas) usando SOMENTE os
               SKUs do catálogo abaixo. Nunca invente SKU nem preço. Se o produto exato não existir, use o mais próximo.
            3. Calcular quantidades: 2 suportes mão-francesa por prateleira; tinta: a área de PAREDE de um cômodo
               é cerca de 3 vezes a área do piso (some o teto se o cliente pedir), galão 3,6L rende ~38 m² com 2 demãos
               e lata 18L ~190 m² com 2 demãos; escolha a combinação mais barata que cubra a área; porcelanato: área + 10% de perda; argamassa: 1 saco de 20kg a cada 4 m²;
               espaçador: 1 pacote a cada 10 m²; rejunte: 1kg a cada 5 m².
            4. Preferir marca própria (marcada [MP]) quando for equivalente.
            5. Ferramentas que o cliente talvez já tenha (furadeira, nível, chaves) entram com "essencial": false.
            6. "precisaEspecialista": true somente se a lista tiver item que depende de vendedor (ex.: tinta
               personalizada) ou se o serviço exigir mexer no quadro de energia, gás ou estrutura.
               Trocar chuveiro, torneira ou lâmpada NÃO exige especialista.
            7. Se receber foto, descreva em "diagnostico" o que identificou, ex.: "Mofo na parede (alta confiança)".
            8. Porcelanato é só mostruário: o cliente escolhe na loja e recebe em casa; avise isso na resposta.
            Responda SOMENTE com um objeto JSON neste formato:
            {"resposta": "até 2 frases, tom amigável, sem jargão técnico",
             "tituloProjeto": "nome curto do projeto, ex.: 'Trocar piso da sala (18 m²)'; null só enquanto pergunta",
             "perguntasRapidas": ["até 4 respostas curtas que o cliente pode tocar"],
             "diagnostico": ["..."],
             "itens": [{"sku": "LM0000", "quantidade": 1, "essencial": true, "motivo": "por que este item"}],
             "precisaEspecialista": false}
            Enquanto estiver perguntando, "itens" deve ser []. Quando montar a lista, "perguntasRapidas" deve ser [].
            """;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final CatalogoService catalogo;

    public OpenAiCliente(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    public boolean configurado() {
        return !Config.openAiChave().isBlank();
    }

    /**
     * Retries once on connection-level failures (reset, connect timeout), which are common on
     * store Wi-Fi. A request that timed out after connecting is not retried: the user already waited.
     */
    private HttpResponse<String> enviar(HttpRequest requisicao) throws IOException, InterruptedException {
        try {
            return http.send(requisicao, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            if (e instanceof HttpTimeoutException && !(e instanceof HttpConnectTimeoutException)) {
                throw e;
            }
            return http.send(requisicao, HttpResponse.BodyHandlers.ofString());
        }
    }

    public Sugestao conversar(List<MensagemDTO> mensagens, String imagemBase64) throws IOException, InterruptedException {
        JsonArrayBuilder historico = Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                        .add("role", "system")
                        .add("content", INSTRUCOES + "\nCATÁLOGO DA LOJA:\n" + catalogoComoTexto()));

        for (int i = 0; i < mensagens.size(); i++) {
            MensagemDTO mensagem = mensagens.get(i);
            boolean ultima = i == mensagens.size() - 1;
            String papel = "assistente".equals(mensagem.papel()) ? "assistant" : "user";
            String texto = mensagem.texto() == null ? "" : mensagem.texto();
            if (ultima && "user".equals(papel) && imagemBase64 != null && !imagemBase64.isBlank()) {
                historico.add(Json.createObjectBuilder()
                        .add("role", "user")
                        .add("content", Json.createArrayBuilder()
                                .add(Json.createObjectBuilder().add("type", "text")
                                        .add("text", texto.isBlank() ? "Veja a foto do meu problema." : texto))
                                .add(Json.createObjectBuilder().add("type", "image_url")
                                        .add("image_url", Json.createObjectBuilder().add("url", imagemBase64)))));
            } else {
                historico.add(Json.createObjectBuilder().add("role", papel).add("content", texto));
            }
        }

        String modelo = Config.openAiModelo();
        var builder = Json.createObjectBuilder()
                .add("model", modelo)
                .add("response_format", Json.createObjectBuilder().add("type", "json_object"))
                .add("messages", historico);
        // Reasoning models reject a custom temperature but accept an effort level.
        if (modelo.startsWith("gpt-5") || modelo.startsWith("o")) {
            builder.add("reasoning_effort", "low");
        } else {
            builder.add("temperature", 0.3);
        }
        JsonObject corpo = builder.build();

        HttpRequest requisicao = HttpRequest.newBuilder(ENDPOINT)
                .timeout(Duration.ofSeconds(40))
                .header("Authorization", "Bearer " + Config.openAiChave())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpo.toString()))
                .build();

        HttpResponse<String> resposta = enviar(requisicao);
        if (resposta.statusCode() != 200) {
            throw new IOException("OpenAI returned HTTP " + resposta.statusCode() + ": " + resposta.body());
        }
        String conteudo = lerJson(resposta.body())
                .getJsonArray("choices").getJsonObject(0)
                .getJsonObject("message").getString("content");
        return interpretar(lerJson(conteudo));
    }

    private String catalogoComoTexto() {
        StringBuilder texto = new StringBuilder();
        for (Produto p : catalogo.todos()) {
            Corredor corredor = catalogo.corredor(p.getNumeroCorredor());
            texto.append(p.getSku()).append(" | ").append(p.getNome())
                    .append(" | ").append(p.getMarca()).append(p.isMarcaPropria() ? " [MP]" : "")
                    .append(" | R$ ").append(p.getPreco()).append("/").append(p.getUnidade())
                    .append(" | ").append(corredor.secao().nome())
                    .append(p.getDisponibilidade().name().equals("MOSTRUARIO") ? " | mostruário, entrega em casa" : "")
                    .append(" | ").append(p.getPalavrasChave())
                    .append('\n');
        }
        return texto.toString();
    }

    private static JsonObject lerJson(String texto) {
        try (JsonReader leitor = Json.createReader(new StringReader(texto))) {
            return leitor.readObject();
        }
    }

    private static Sugestao interpretar(JsonObject json) {
        List<Sugestao.Item> itens = new ArrayList<>();
        JsonArray arrayItens = json.containsKey("itens") && !json.isNull("itens") ? json.getJsonArray("itens") : null;
        if (arrayItens != null) {
            for (JsonValue valor : arrayItens) {
                JsonObject item = valor.asJsonObject();
                itens.add(new Sugestao.Item(
                        item.getString("sku", ""),
                        item.containsKey("quantidade") ? item.getJsonNumber("quantidade").intValue() : 1,
                        item.getBoolean("essencial", true),
                        item.getString("motivo", "")));
            }
        }
        String titulo = json.containsKey("tituloProjeto") && !json.isNull("tituloProjeto")
                ? json.getString("tituloProjeto") : null;
        return new Sugestao(
                json.getString("resposta", "Pode me contar um pouco mais?"),
                titulo,
                textos(json, "perguntasRapidas"),
                textos(json, "diagnostico"),
                itens,
                json.getBoolean("precisaEspecialista", false));
    }

    private static List<String> textos(JsonObject json, String campo) {
        if (!json.containsKey(campo) || json.isNull(campo)) {
            return List.of();
        }
        List<String> lista = new ArrayList<>();
        for (JsonValue valor : json.getJsonArray(campo)) {
            if (valor.getValueType() == JsonValue.ValueType.STRING) {
                lista.add(((jakarta.json.JsonString) valor).getString());
            }
        }
        return lista;
    }
}
