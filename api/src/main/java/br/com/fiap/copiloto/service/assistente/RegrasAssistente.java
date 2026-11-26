package br.com.fiap.copiloto.service.assistente;

import br.com.fiap.copiloto.dto.MensagemDTO;
import br.com.fiap.copiloto.model.Produto;
import br.com.fiap.copiloto.service.CatalogoService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Offline fallback used when the AI is not configured or fails, so the in-store
 * flow never stops. Covers the most common DIY projects with fixed recipes.
 */
public class RegrasAssistente {

    private static final Pattern AREA = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(?:m2|m²|m\\b|metros?)");
    private static final Pattern NUMERO_ISOLADO = Pattern.compile("^\\s*~?(\\d+(?:[.,]\\d+)?)\\s*$");
    private static final Pattern QUANTIDADE_PRATELEIRAS = Pattern.compile("(\\d+|duas|tres|quatro)\\s+prateleiras?");

    private static final List<String> SUGESTOES_INICIAIS = List.of(
            "Minha torneira está pingando", "Quero instalar uma prateleira", "Vou pintar um quarto", "Trocar o chuveiro");

    private final CatalogoService catalogo;

    public RegrasAssistente(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    public Sugestao responder(List<MensagemDTO> mensagens, boolean temFoto) {
        String conversa = CatalogoService.normalizar(textoDoCliente(mensagens));
        String ultima = CatalogoService.normalizar(ultimaDoCliente(mensagens));

        if (temFoto && conversa.isBlank()) {
            return pergunta("Recebi sua foto! Para eu acertar os produtos, me diga em poucas palavras o que aconteceu.",
                    List.of("Mofo na parede", "Rachadura na parede", "Vazamento", "Peça quebrada"));
        }
        if (contem(conversa, "mofo", "bolor", "umidade")) {
            return mofo();
        }
        if (contem(conversa, "rachadura", "trinca", "fissura")) {
            return rachadura();
        }
        if (contem(conversa, "torneira")) {
            return torneira(conversa);
        }
        if (contem(conversa, "chuveiro")) {
            return chuveiro(conversa, ultima);
        }
        if (contem(conversa, "prateleira", "mao francesa", "mao-francesa")) {
            return prateleira(conversa);
        }
        if (contem(conversa, "porcelanato", "piso", "revestimento", "assentar")) {
            return piso(conversa, ultima);
        }
        if (contem(conversa, "pintar", "pintura", "tinta")) {
            return pintura(conversa, ultima);
        }
        if (contem(conversa, "entupi", "entupido", "desentup")) {
            return lista("Desentupir pia ou ralo", "Para desentupir, comece pelo desentupidor manual. Se o sifão estiver rachado, vale trocar também.",
                    item("LM5008", 1, true, "Resolve a maioria dos entupimentos de pia e ralo."),
                    item("LM5005", 1, false, "Troque se o sifão estiver vazando ou rachado."));
        }
        if (contem(conversa, "plafon", "luminaria", "pendente")) {
            return lista("Instalar luminária", "Montei o que você precisa para instalar um plafon. Desligue o disjuntor antes de começar!",
                    item("LM3003", 1, true, "Luminária de teto pronta para instalar."),
                    item("LM8006", 1, true, "Fixa a base no teto."),
                    item("LM4001", 1, true, "Isola as emendas dos fios."),
                    item("LM4007", 1, false, "Confirma que a energia está desligada."),
                    item("LM9002", 1, false, "Broca para a bucha S6."));
        }
        if (contem(conversa, "lampada", "luz queimou", "queimou a luz")) {
            return lista("Trocar lâmpada", "Para trocar a lâmpada, a LED de 9W ilumina como uma comum de 60W e gasta bem menos.",
                    item("LM3001", 1, true, "Equivale a 60W, bivolt, rosca comum E27."));
        }
        if (contem(conversa, "cortina", "varao")) {
            return lista("Instalar cortina", "Aqui está o kit completo para instalar sua cortina.",
                    item("LM2001", 1, true, "Cortina blackout 2,80 x 2,30m."),
                    item("LM2002", 1, true, "Varão de 1,5m com suportes."),
                    item("LM8006", 1, true, "Fixa os suportes do varão."),
                    item("LM9002", 1, true, "Broca para a bucha S6."),
                    item("LM9004", 1, false, "Se você não tiver furadeira."));
        }

        List<Produto> encontrados = catalogo.pesquisar(conversa, 3);
        if (!encontrados.isEmpty()) {
            List<Sugestao.Item> itens = encontrados.stream()
                    .map(p -> item(p.getSku(), 1, true, "Combina com o que você procurou."))
                    .toList();
            return new Sugestao("Encontrei estes produtos. Se me contar o que vai fazer, eu monto a lista completa.",
                    "Minha busca", List.of(), List.of(), itens, false);
        }
        return pergunta("Ainda não entendi o que você precisa. Pode me contar o que quer consertar ou fazer?",
                SUGESTOES_INICIAIS);
    }

    private Sugestao torneira(String conversa) {
        boolean consertar = contem(conversa, "consert", "reparo", "courinho");
        boolean trocar = contem(conversa, "trocar a torneira", "torneira nova", "comprar torneira", "trocar torneira");
        boolean cozinha = contem(conversa, "cozinha");
        if (!consertar && !trocar) {
            return pergunta("Se ela pinga mesmo fechada, quase sempre basta trocar o reparo (courinho), que é bem mais barato. Prefere consertar ou trocar a torneira?",
                    List.of("Consertar trocando o reparo", "Trocar a torneira do banheiro", "Trocar a torneira da cozinha"));
        }
        if (consertar && !trocar) {
            return lista("Consertar torneira pingando", "Para consertar: feche o registro, troque o reparo e passe veda rosca na rosca.",
                    item("LM5001", 1, true, "Substitui o vedante gasto que faz a torneira pingar."),
                    item("LM5003", 1, true, "Garante a vedação ao montar de volta."),
                    item("LM9008", 1, false, "Para soltar a torneira, se você não tiver."));
        }
        if (cozinha) {
            return lista("Trocar torneira da cozinha", "Montei a lista para trocar a torneira da cozinha.",
                    item("LM6002", 1, true, "Torneira de parede com bica móvel."),
                    item("LM5003", 1, true, "Veda a rosca e evita vazamento."),
                    item("LM9007", 1, false, "Para soltar a torneira antiga."));
        }
        return lista("Trocar torneira do banheiro", "Montei a lista para trocar a torneira do banheiro.",
                item("LM6001", 1, true, "Torneira de bancada da nossa marca Sensea."),
                item("LM5004", 1, true, "Liga o registro da parede à torneira nova."),
                item("LM5003", 1, true, "Veda a rosca e evita vazamento."),
                item("LM9008", 1, false, "Para soltar a torneira antiga."));
    }

    private Sugestao chuveiro(String conversa, String ultima) {
        boolean v127 = contem(conversa, "127", "110");
        boolean v220 = contem(conversa, "220");
        if (contem(ultima, "nao sei")) {
            return new Sugestao("Sem problema! A voltagem costuma estar na etiqueta do disjuntor ou no chuveiro antigo. Posso chamar um especialista para te ajudar a confirmar.",
                    null, List.of("127V", "220V"), List.of(), List.of(item("LM4007", 1, true, "Ajuda a conferir a fiação com segurança.")), true);
        }
        if (!v127 && !v220) {
            return pergunta("Qual é a voltagem da sua casa? Isso muda o chuveiro certo.", List.of("127V", "220V", "Não sei"));
        }
        String chuveiro = v220 ? "LM6005" : "LM6004";
        return lista("Trocar chuveiro", "Aqui está tudo para trocar o chuveiro. Desligue o disjuntor antes de mexer nos fios!",
                item(chuveiro, 1, true, v220 ? "Chuveiro 220V da nossa marca Sensea." : "Chuveiro para rede 127V."),
                item("LM4001", 1, true, "Isola as emendas dos fios."),
                item("LM5003", 1, true, "Veda a rosca do cano."),
                item("LM4007", 1, false, "Confirma que a energia está desligada."));
    }

    private Sugestao prateleira(String conversa) {
        int quantidade = quantidadePrateleiras(conversa);
        boolean drywall = contem(conversa, "drywall", "gesso");
        String bucha = drywall ? "LM8005" : "LM8002";
        String motivoBucha = drywall ? "Parede de gesso precisa de bucha específica." : "Para fixar os suportes em alvenaria.";
        List<Sugestao.Item> itens = new ArrayList<>(List.of(
                item("LM7001", quantidade, true, "Prateleira de pinus 80 x 20cm."),
                item("LM7003", quantidade * 2, true, "2 suportes por prateleira."),
                item(bucha, 1, true, motivoBucha),
                item("LM8003", 1, true, "Parafuso compatível com a bucha.")));
        if (!drywall) {
            itens.add(item("LM9001", 1, true, "Bucha S8 pede furo com broca de 8mm."));
        }
        itens.add(item("LM9005", 1, false, "Para a prateleira não ficar torta."));
        itens.add(item("LM9004", 1, false, "Se você não tiver furadeira."));
        String titulo = quantidade > 1 ? "Instalar " + quantidade + " prateleiras" : "Instalar prateleira";
        return new Sugestao("Montei sua lista com tudo para instalar " + (quantidade > 1 ? "as prateleiras" : "a prateleira") + ", já com as quantidades certas.",
                titulo, List.of(), List.of(), itens, false);
    }

    private Sugestao pintura(String conversa, String ultima) {
        Double area = area(conversa, ultima);
        if (area == null) {
            return pergunta("Ótimo! Qual o tamanho aproximado das paredes? Assim eu calculo a tinta certa.",
                    List.of("Um quarto (~30 m²)", "Uma sala (~50 m²)", "Um apartamento (~150 m²)"));
        }
        double areaTotal = area * 2;
        List<Sugestao.Item> itens = new ArrayList<>();
        if (areaTotal > 300) {
            itens.add(item("LM10001", (int) Math.ceil(areaTotal / 380), true, "Lata 18L: rende ~380 m² por demão, já com 2 demãos."));
        } else {
            itens.add(item("LM10002", (int) Math.ceil(areaTotal / 76), true, "Galão 3,6L: rende ~76 m² por demão, já com 2 demãos."));
        }
        itens.add(item("LM10006", 1, true, "Rolo para aplicar a tinta."));
        itens.add(item("LM10007", 1, true, "Bandeja para o rolo."));
        itens.add(item("LM10008", 1, true, "Protege rodapés e batentes."));
        itens.add(item("LM10014", 1, true, "Para recortes e cantos."));
        itens.add(item("LM10012", 1, false, "Protege piso e móveis."));
        itens.add(item("LM10009", 4, false, "Lixe antes de pintar para um acabamento melhor."));
        return new Sugestao("Para " + formatar(area) + " m² de parede calculei 2 demãos. Lista pronta!",
                "Pintar " + formatar(area) + " m² de parede", List.of(), List.of(), itens, false);
    }

    private Sugestao piso(String conversa, String ultima) {
        Double area = area(conversa, ultima);
        if (area == null) {
            return pergunta("Qual o tamanho do ambiente? Com a área eu calculo piso, argamassa e rejunte.",
                    List.of("Banheiro (~5 m²)", "Cozinha (~10 m²)", "Sala (~20 m²)"));
        }
        int metros = (int) Math.ceil(area * 1.1);
        return new Sugestao("O porcelanato você vê aqui no mostruário e recebe em casa. Já incluí 10% de perda nos cortes.",
                "Assentar " + formatar(area) + " m² de porcelanato", List.of(), List.of(), List.of(
                item("LM11001", metros, true, formatar(area) + " m² + 10% de perda nos cortes."),
                item("LM12002", (int) Math.ceil(area / 4), true, "Porcelanato exige argamassa AC-III: 1 saco a cada 4 m²."),
                item("LM11004", (int) Math.ceil(area / 10), true, "Espaçadores para juntas alinhadas."),
                item("LM11006", (int) Math.ceil(area / 5), true, "Rejunte: 1kg a cada 5 m²."),
                item("LM9010", 1, false, "Para espalhar a argamassa."),
                item("LM11005", 1, false, "Evita degraus entre as peças.")), true);
    }

    private Sugestao mofo() {
        return new Sugestao("Mofo vem de umidade: primeiro remova, depois pinte com tinta antimofo para ele não voltar.",
                "Tratar mofo na parede", List.of(), List.of("Mofo na parede"), List.of(
                item("LM10011", 1, true, "Elimina o mofo antes da pintura."),
                item("LM10004", 1, true, "Tinta com fungicida para áreas úmidas."),
                item("LM10006", 1, true, "Rolo para aplicar a tinta."),
                item("LM10007", 1, true, "Bandeja para o rolo."),
                item("LM12005", 1, false, "Se a umidade vier de infiltração, impermeabilize.")), false);
    }

    private Sugestao rachadura() {
        return new Sugestao("Para trincas finas, lixe, aplique a massa acrílica e pinte por cima. Rachaduras largas ou que crescem precisam de avaliação de um profissional.",
                "Corrigir trinca na parede", List.of(), List.of("Trinca na parede"), List.of(
                item("LM10013", 1, true, "Selante elástico que acompanha a movimentação da parede."),
                item("LM10009", 2, true, "Para preparar a área."),
                item("LM10002", 1, false, "Para repintar a parede depois.")), false);
    }

    private static Sugestao pergunta(String texto, List<String> opcoes) {
        return new Sugestao(texto, null, opcoes, List.of(), List.of(), false);
    }

    private static Sugestao lista(String titulo, String texto, Sugestao.Item... itens) {
        return new Sugestao(texto, titulo, List.of(), List.of(), List.of(itens), false);
    }

    private static Sugestao.Item item(String sku, int quantidade, boolean essencial, String motivo) {
        return new Sugestao.Item(sku, Math.max(1, quantidade), essencial, motivo);
    }

    private static boolean contem(String texto, String... termos) {
        for (String termo : termos) {
            if (texto.contains(termo)) {
                return true;
            }
        }
        return false;
    }

    private static Double area(String conversa, String ultima) {
        Matcher isolado = NUMERO_ISOLADO.matcher(ultima);
        if (isolado.find()) {
            return Double.parseDouble(isolado.group(1).replace(',', '.'));
        }
        Matcher comUnidade = AREA.matcher(conversa);
        Double encontrada = null;
        while (comUnidade.find()) {
            encontrada = Double.parseDouble(comUnidade.group(1).replace(',', '.'));
        }
        return encontrada;
    }

    private static int quantidadePrateleiras(String conversa) {
        Matcher m = QUANTIDADE_PRATELEIRAS.matcher(conversa);
        if (!m.find()) {
            return 1;
        }
        return switch (m.group(1)) {
            case "duas" -> 2;
            case "tres" -> 3;
            case "quatro" -> 4;
            default -> Math.min(10, Integer.parseInt(m.group(1)));
        };
    }

    private static String formatar(double valor) {
        return valor == Math.floor(valor) ? String.valueOf((long) valor) : String.valueOf(valor).replace('.', ',');
    }

    private static String textoDoCliente(List<MensagemDTO> mensagens) {
        StringBuilder texto = new StringBuilder();
        for (MensagemDTO mensagem : mensagens) {
            if (!"assistente".equals(mensagem.papel()) && mensagem.texto() != null) {
                texto.append(mensagem.texto()).append(' ');
            }
        }
        return texto.toString();
    }

    private static String ultimaDoCliente(List<MensagemDTO> mensagens) {
        for (int i = mensagens.size() - 1; i >= 0; i--) {
            if (!"assistente".equals(mensagens.get(i).papel())) {
                String texto = mensagens.get(i).texto();
                return texto == null ? "" : texto;
            }
        }
        return "";
    }
}
