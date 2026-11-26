"""Gera o database/02_seed.sql com a loja simulada (grafo do mapa, catálogo e estoque).

Execução: python3 database/gerador/gerar_seed.py
"""
from pathlib import Path

SECOES = [
    (1, "Jardim", "#4CAF50"),
    (2, "Decoração", "#E91E63"),
    (3, "Iluminação", "#FFC107"),
    (4, "Elétrica", "#FF9800"),
    (5, "Hidráulica", "#2196F3"),
    (6, "Banheiro e Metais", "#00BCD4"),
    (7, "Madeiras e Organização", "#8D6E63"),
    (8, "Fixação e Ferragens", "#607D8B"),
    (9, "Ferramentas", "#F44336"),
    (10, "Tintas", "#9C27B0"),
    (11, "Pisos e Revestimentos", "#795548"),
    (12, "Materiais Básicos", "#9E9E9E"),
]

# Escala do mapa: 10 px = 1 m. Corredores na vertical; corredor da frente embaixo, do fundo em cima.
CORREDOR_X0, CORREDOR_DX = 70, 78
Y_FRENTE, Y_MEIO, Y_FUNDO = 520, 300, 80

# sku, nome, descrição, marca, marca própria, preço, unidade, corredor, prateleira, tipo de atendimento,
# disponibilidade, palavras-chave, estoque do sistema, estoque na prateleira
PRODUTOS = [
    ("LM1001", "Mangueira 15m com esguicho", "Mangueira flexível antitorção com esguicho de 3 jatos.", "Naterial", "S", 79.90, "un", 1, 2, "AUTONOMO", "PRONTA_ENTREGA", "jardim regar mangueira", 30, 18),
    ("LM1002", "Vaso autoirrigável 30cm", "Vaso plástico com reservatório de água.", "Naterial", "S", 49.90, "un", 1, 3, "AUTONOMO", "PRONTA_ENTREGA", "vaso planta jardim", 25, 12),
    ("LM1003", "Terra vegetal adubada 20kg", "Substrato pronto para vasos e canteiros.", "Naterial", "S", 19.90, "saco", 1, 1, "AUTONOMO", "PRONTA_ENTREGA", "terra adubo planta", 80, 40),

    ("LM2001", "Cortina blackout 2,80 x 2,30m", "Bloqueia até 100% da luz, com ilhós.", "Inspire", "S", 129.90, "un", 2, 3, "AUTONOMO", "PRONTA_ENTREGA", "cortina blackout quarto janela", 20, 9),
    ("LM2002", "Varão para cortina 1,5m com suportes", "Varão de alumínio 19mm com 2 suportes e ponteiras.", "Inspire", "S", 49.90, "un", 2, 2, "AUTONOMO", "PRONTA_ENTREGA", "varão cortina instalar", 30, 14),
    ("LM2003", "Espelho redondo 60cm", "Espelho com moldura metálica preta.", "Inspire", "S", 179.90, "un", 2, 4, "AUTONOMO", "PRONTA_ENTREGA", "espelho decoração banheiro sala", 10, 4),
    ("LM2004", "Quadro decorativo 40 x 60cm", "Impressão em canvas com moldura.", "Inspire", "S", 69.90, "un", 2, 3, "AUTONOMO", "PRONTA_ENTREGA", "quadro decoração parede", 15, 8),

    ("LM3001", "Lâmpada LED bulbo 9W branca fria", "Equivale a 60W incandescente, bivolt, E27.", "Lexman", "S", 7.90, "un", 3, 2, "AUTONOMO", "PRONTA_ENTREGA", "lâmpada led luz queimou", 300, 150),
    ("LM3002", "Lâmpada LED bulbo 9W Philips", "Bivolt, E27, 6500K.", "Philips", "N", 12.90, "un", 3, 2, "AUTONOMO", "PRONTA_ENTREGA", "lâmpada led philips", 60, 0),
    ("LM3003", "Plafon LED de sobrepor 18W redondo", "Luminária de teto pronta para instalar, bivolt.", "Inspire", "S", 49.90, "un", 3, 3, "APOIO_PARCIAL", "PRONTA_ENTREGA", "plafon luminária teto led", 40, 16),
    ("LM3004", "Luminária pendente industrial preta", "Pendente de metal para cozinha ou sala, E27.", "Inspire", "S", 129.90, "un", 3, 4, "APOIO_PARCIAL", "PRONTA_ENTREGA", "pendente luminária cozinha", 12, 5),
    ("LM3005", "Fita LED 5m com fonte", "Fita LED branca quente com fonte e adesivo.", "Inspire", "S", 89.90, "un", 3, 1, "AUTONOMO", "PRONTA_ENTREGA", "fita led iluminação móvel", 25, 10),

    ("LM4001", "Fita isolante 19mm x 10m", "Fita isolante antichama preta.", "Lexman", "S", 6.90, "un", 4, 1, "AUTONOMO", "PRONTA_ENTREGA", "fita isolante elétrica emenda fio", 200, 90),
    ("LM4002", "Fita isolante Imperial 19mm x 20m", "Fita isolante profissional.", "3M", "N", 14.90, "un", 4, 1, "AUTONOMO", "PRONTA_ENTREGA", "fita isolante 3m", 50, 22),
    ("LM4003", "Cabo flexível 6mm² (metro)", "Cabo de cobre para chuveiro e circuitos de alta corrente.", "Lexman", "S", 7.90, "m", 4, 3, "APOIO_PARCIAL", "PRONTA_ENTREGA", "fio cabo 6mm chuveiro", 500, 300),
    ("LM4004", "Disjuntor DIN 40A", "Disjuntor monopolar curva C.", "Lexman", "S", 29.90, "un", 4, 2, "APOIO_PARCIAL", "PRONTA_ENTREGA", "disjuntor quadro chuveiro desarma", 40, 18),
    ("LM4005", "Tomada 2P+T 10A branca", "Módulo de tomada padrão brasileiro com placa.", "Lexman", "S", 12.90, "un", 4, 2, "AUTONOMO", "PRONTA_ENTREGA", "tomada trocar", 120, 60),
    ("LM4006", "Interruptor simples branco", "Interruptor 1 tecla com placa 4x2.", "Lexman", "S", 11.90, "un", 4, 2, "AUTONOMO", "PRONTA_ENTREGA", "interruptor tecla luz", 100, 45),
    ("LM4007", "Chave teste de tensão", "Detecta tensão de 100 a 500V antes de mexer na fiação.", "Lexman", "S", 15.90, "un", 4, 4, "AUTONOMO", "PRONTA_ENTREGA", "chave teste segurança elétrica", 60, 25),

    ("LM5001", "Reparo universal para torneira 1/2\"", "Kit com vedante (courinho) para torneiras de pressão que pingam.", "Boutte", "S", 12.90, "un", 5, 2, "AUTONOMO", "PRONTA_ENTREGA", "torneira pingando vazando reparo courinho vedante", 80, 35),
    ("LM5002", "Fita veda rosca 18mm x 25m", "Fita PTFE para vedar conexões roscáveis.", "Tigre", "N", 8.90, "un", 5, 1, "AUTONOMO", "PRONTA_ENTREGA", "veda rosca vazamento rosca", 150, 70),
    ("LM5003", "Fita veda rosca 18mm x 10m", "Fita PTFE para vedar conexões roscáveis.", "Boutte", "S", 5.50, "un", 5, 1, "AUTONOMO", "PRONTA_ENTREGA", "veda rosca vazamento rosca", 150, 80),
    ("LM5004", "Engate flexível 40cm 1/2\"", "Liga o registro da parede à torneira ou misturador.", "Boutte", "S", 19.90, "un", 5, 3, "AUTONOMO", "PRONTA_ENTREGA", "flexível engate ligação torneira", 90, 40),
    ("LM5005", "Sifão sanfonado universal", "Sifão extensível para pia e tanque.", "Tigre", "N", 24.90, "un", 5, 3, "AUTONOMO", "PRONTA_ENTREGA", "sifão pia vazando cano", 70, 30),
    ("LM5006", "Cola para PVC 75g", "Adesivo para tubos e conexões de PVC soldável.", "Tigre", "N", 14.90, "un", 5, 2, "AUTONOMO", "PRONTA_ENTREGA", "cola pvc cano", 80, 32),
    ("LM5007", "Registro de gaveta 3/4\"", "Registro bruto para instalação embutida.", "Docol", "N", 89.90, "un", 5, 4, "APOIO_PARCIAL", "PRONTA_ENTREGA", "registro água", 25, 10),
    ("LM5008", "Desentupidor manual", "Desentupidor de borracha para pia e vaso.", "Boutte", "S", 19.90, "un", 5, 1, "AUTONOMO", "PRONTA_ENTREGA", "entupido desentupir pia vaso ralo", 60, 25),

    ("LM6001", "Torneira de bancada para banheiro bica baixa", "Torneira cromada 1/4 de volta, para pia de banheiro.", "Sensea", "S", 89.90, "un", 6, 3, "AUTONOMO", "PRONTA_ENTREGA", "torneira banheiro bancada pia lavatório", 40, 15),
    ("LM6002", "Torneira de parede para cozinha bica móvel", "Torneira cromada com arejador, para pia de cozinha.", "Docol", "N", 149.90, "un", 6, 3, "AUTONOMO", "PRONTA_ENTREGA", "torneira cozinha parede pia", 20, 8),
    ("LM6003", "Torneira de bancada Pressmatic", "Torneira de fechamento automático para banheiro.", "Docol", "N", 219.90, "un", 6, 4, "APOIO_PARCIAL", "PRONTA_ENTREGA", "torneira banheiro automática", 6, 0),
    ("LM6004", "Chuveiro elétrico 5500W 127V", "Chuveiro com 4 temperaturas, para rede 127V.", "Lorenzetti", "N", 129.90, "un", 6, 2, "APOIO_PARCIAL", "PRONTA_ENTREGA", "chuveiro queimou trocar 127v", 30, 12),
    ("LM6005", "Chuveiro elétrico 6800W 220V", "Chuveiro com 4 temperaturas, para rede 220V.", "Sensea", "S", 119.90, "un", 6, 2, "APOIO_PARCIAL", "PRONTA_ENTREGA", "chuveiro queimou trocar 220v", 35, 14),
    ("LM6006", "Ducha higiênica com registro", "Ducha cromada com gatilho e registro.", "Sensea", "S", 79.90, "un", 6, 3, "AUTONOMO", "PRONTA_ENTREGA", "ducha higiênica banheiro", 25, 10),
    ("LM6007", "Assento sanitário universal branco", "Assento de polipropileno com fixação universal.", "Sensea", "S", 59.90, "un", 6, 1, "AUTONOMO", "PRONTA_ENTREGA", "assento vaso privada tampa", 30, 14),

    ("LM7001", "Prateleira pinus 80 x 20cm", "Tábua de pinus aparelhada, suporta até 15kg com 2 suportes.", "Spaceo", "S", 38.00, "un", 7, 2, "AUTONOMO", "PRONTA_ENTREGA", "prateleira madeira parede instalar", 40, 12),
    ("LM7002", "Prateleira MDF branca 60 x 20cm", "Prateleira de MDF com acabamento branco.", "Spaceo", "S", 34.90, "un", 7, 2, "AUTONOMO", "PRONTA_ENTREGA", "prateleira branca mdf", 35, 14),
    ("LM7003", "Suporte mão-francesa 25cm", "Suporte de aço para prateleira, vendido por unidade.", "Spaceo", "S", 16.00, "un", 7, 3, "AUTONOMO", "PRONTA_ENTREGA", "suporte mão francesa prateleira", 120, 24),
    ("LM7004", "Kit prateleira invisível 60cm", "Prateleira com fixação oculta.", "Spaceo", "S", 69.90, "un", 7, 4, "AUTONOMO", "PRONTA_ENTREGA", "prateleira invisível", 0, 0),
    ("LM7005", "Nicho MDF 30 x 30cm", "Nicho quadrado para parede.", "Spaceo", "S", 44.90, "un", 7, 3, "AUTONOMO", "PRONTA_ENTREGA", "nicho organização parede", 20, 9),

    ("LM8001", "Bucha S8 (cartela 20 un)", "Bucha de nylon para alvenaria e concreto, use broca 8mm.", "Fischer", "N", 12.00, "cartela", 8, 2, "AUTONOMO", "PRONTA_ENTREGA", "bucha s8 parede fixar", 40, 0),
    ("LM8002", "Bucha S8 com anel (cartela 25 un)", "Bucha de nylon para alvenaria, use broca 8mm.", "Axton", "S", 9.90, "cartela", 8, 2, "AUTONOMO", "PRONTA_ENTREGA", "bucha s8 parede fixar", 60, 28),
    ("LM8003", "Parafuso 4,5 x 40mm (caixa 50 un)", "Parafuso cabeça chata Phillips, compatível com bucha S8.", "Axton", "S", 8.00, "caixa", 8, 3, "AUTONOMO", "PRONTA_ENTREGA", "parafuso bucha s8", 80, 35),
    ("LM8004", "Parafuso 5 x 50mm (caixa 50 un)", "Parafuso cabeça chata Phillips para cargas maiores.", "Axton", "S", 9.50, "caixa", 8, 3, "AUTONOMO", "PRONTA_ENTREGA", "parafuso bucha s10", 70, 30),
    ("LM8005", "Bucha para drywall (cartela 10 un)", "Bucha basculante para paredes de gesso acartonado.", "Fischer", "N", 18.90, "cartela", 8, 4, "APOIO_PARCIAL", "PRONTA_ENTREGA", "drywall gesso bucha parede oca", 40, 16),
    ("LM8006", "Bucha S6 com parafuso (kit 20 un)", "Kit bucha S6 + parafuso para cargas leves, use broca 6mm.", "Axton", "S", 9.90, "kit", 8, 2, "AUTONOMO", "PRONTA_ENTREGA", "bucha s6 parafuso fixar leve", 90, 40),
    ("LM8007", "Fita dupla face fixa forte 19mm x 2m", "Fixa objetos de até 1kg sem furar.", "3M", "N", 24.90, "un", 8, 1, "AUTONOMO", "PRONTA_ENTREGA", "fixar sem furar dupla face", 50, 20),

    ("LM9001", "Broca para concreto 8mm", "Broca de vídia para alvenaria, para bucha S8.", "Dexter", "S", 15.00, "un", 9, 2, "AUTONOMO", "PRONTA_ENTREGA", "broca 8mm parede furar", 50, 20),
    ("LM9002", "Broca para concreto 6mm", "Broca de vídia para alvenaria, para bucha S6.", "Dexter", "S", 12.90, "un", 9, 2, "AUTONOMO", "PRONTA_ENTREGA", "broca 6mm parede furar", 50, 22),
    ("LM9003", "Kit brocas multiuso 5 peças", "Brocas de 4 a 10mm para madeira, metal e alvenaria.", "Dexter", "S", 39.90, "kit", 9, 2, "AUTONOMO", "PRONTA_ENTREGA", "broca kit furar", 30, 12),
    ("LM9004", "Furadeira de impacto 650W", "Furadeira com mandril 1/2\" e função impacto, 127V.", "Dexter", "S", 199.90, "un", 9, 4, "APOIO_PARCIAL", "PRONTA_ENTREGA", "furadeira furar parede", 15, 6),
    ("LM9005", "Nível de bolha 30cm", "Nível de alumínio com 3 bolhas.", "Dexter", "S", 22.00, "un", 9, 3, "AUTONOMO", "PRONTA_ENTREGA", "nível alinhar prateleira quadro", 40, 18),
    ("LM9006", "Trena 5m", "Trena emborrachada com trava.", "Dexter", "S", 19.90, "un", 9, 3, "AUTONOMO", "PRONTA_ENTREGA", "trena medir", 50, 25),
    ("LM9007", "Chave grifo 10\"", "Chave para tubos e conexões hidráulicas.", "Dexter", "S", 39.90, "un", 9, 3, "AUTONOMO", "PRONTA_ENTREGA", "chave grifo cano hidráulica", 20, 9),
    ("LM9008", "Chave inglesa ajustável 8\"", "Chave ajustável cromada até 25mm.", "Dexter", "S", 34.90, "un", 9, 3, "AUTONOMO", "PRONTA_ENTREGA", "chave inglesa torneira porca", 30, 12),
    ("LM9009", "Jogo de chaves de fenda e Phillips 6 peças", "Chaves com ponta imantada.", "Dexter", "S", 29.90, "jogo", 9, 1, "AUTONOMO", "PRONTA_ENTREGA", "chave fenda phillips parafusar", 40, 20),
    ("LM9010", "Desempenadeira dentada 8mm", "Para espalhar argamassa no assentamento de pisos.", "Dexter", "S", 24.90, "un", 9, 4, "AUTONOMO", "PRONTA_ENTREGA", "desempenadeira argamassa piso assentar", 30, 12),
    ("LM9011", "Parafusadeira e furadeira a bateria 12V", "Sem fio, com 2 baterias, carregador e controle de torque. Fura madeira e metal.", "Dexter", "S", 249.90, "un", 9, 4, "APOIO_PARCIAL", "PRONTA_ENTREGA", "parafusadeira furadeira bateria sem fio parafusar montar móvel", 15, 6),
    ("LM9012", "Jogo de bits para parafusadeira 32 peças", "Pontas Phillips, fenda, Torx e Allen com adaptador magnético.", "Dexter", "S", 34.90, "jogo", 9, 1, "AUTONOMO", "PRONTA_ENTREGA", "bits pontas parafusadeira phillips fenda torx allen", 40, 15),
    ("LM9013", "Serra tico-tico 500W", "Corta madeira, MDF e metal fino, em retas e curvas. 127V.", "Dexter", "S", 229.90, "un", 9, 4, "APOIO_PARCIAL", "PRONTA_ENTREGA", "serra tico-tico cortar madeira mdf recorte", 10, 4),
    ("LM9014", "Escada de alumínio 5 degraus", "Escada doméstica com plataforma e trava de segurança, até 120kg.", "Dexter", "S", 189.90, "un", 9, 5, "AUTONOMO", "PRONTA_ENTREGA", "escada degrau alcançar teto pintar lâmpada", 12, 5),
    ("LM9015", "Trena a laser 30m", "Mede distância, área e volume com um toque.", "Dexter", "S", 149.90, "un", 9, 3, "AUTONOMO", "PRONTA_ENTREGA", "trena laser medir distância área metragem cômodo", 12, 5),
    ("LM9016", "Óculos de proteção incolor", "Protege os olhos de pó e lascas ao furar, cortar e lixar.", "Dexter", "S", 14.90, "un", 9, 1, "AUTONOMO", "PRONTA_ENTREGA", "óculos proteção segurança furar cortar lixar", 40, 20),

    ("LM10001", "Tinta acrílica fosca branco neve 18L", "Rende até 380m² por demão, lavável.", "Luxens", "S", 289.90, "lata", 10, 1, "AUTONOMO", "PRONTA_ENTREGA", "tinta parede pintar branco", 40, 15),
    ("LM10002", "Tinta acrílica fosca branco neve 3,6L", "Rende até 76m² por demão, lavável.", "Luxens", "S", 89.90, "galão", 10, 2, "AUTONOMO", "PRONTA_ENTREGA", "tinta parede pintar branco", 60, 24),
    ("LM10003", "Tinta acrílica Suvinil fosco completo 18L", "Tinta premium, rende até 500m² por demão.", "Suvinil", "N", 419.90, "lata", 10, 1, "AUTONOMO", "PRONTA_ENTREGA", "tinta suvinil parede", 12, 0),
    ("LM10004", "Tinta antimofo branca 3,6L", "Com fungicida, indicada para banheiros e áreas úmidas.", "Luxens", "S", 89.90, "galão", 10, 2, "AUTONOMO", "PRONTA_ENTREGA", "tinta antimofo mofo banheiro umidade", 30, 12),
    ("LM10005", "Tinta personalizada na máquina de cores 3,6L", "Cor feita na hora pelo especialista a partir de uma amostra.", "Luxens", "S", 119.90, "galão", 10, 4, "DEPENDE_VENDEDOR", "PRONTA_ENTREGA", "cor personalizada tinta colorida", 99, 99),
    ("LM10006", "Rolo de lã 23cm com cabo", "Rolo para paredes e tetos com acabamento liso.", "Luxens", "S", 29.90, "un", 10, 3, "AUTONOMO", "PRONTA_ENTREGA", "rolo pintar parede", 50, 20),
    ("LM10007", "Bandeja para pintura", "Bandeja plástica para rolos de até 23cm.", "Luxens", "S", 12.90, "un", 10, 3, "AUTONOMO", "PRONTA_ENTREGA", "bandeja pintura rolo", 60, 30),
    ("LM10008", "Fita crepe 48mm x 50m", "Protege rodapés e batentes durante a pintura.", "Adere", "N", 14.90, "un", 10, 3, "AUTONOMO", "PRONTA_ENTREGA", "fita crepe pintura proteger", 80, 40),
    ("LM10009", "Lixa para parede grão 120", "Folha de lixa para preparar a parede.", "Norton", "N", 2.50, "folha", 10, 3, "AUTONOMO", "PRONTA_ENTREGA", "lixa lixar parede", 300, 150),
    ("LM10010", "Massa corrida PVA 3,6L", "Corrige imperfeições antes da pintura em áreas internas.", "Luxens", "S", 39.90, "galão", 10, 2, "AUTONOMO", "PRONTA_ENTREGA", "massa corrida parede buraco", 40, 18),
    ("LM10011", "Removedor de mofo 500ml", "Elimina mofo e bolor antes da pintura.", "Luxens", "S", 24.90, "un", 10, 4, "AUTONOMO", "PRONTA_ENTREGA", "mofo bolor remover limpar", 50, 20),
    ("LM10012", "Lona plástica 4 x 5m", "Protege móveis e piso durante a pintura.", "Luxens", "S", 19.90, "un", 10, 3, "AUTONOMO", "PRONTA_ENTREGA", "lona proteger pintura", 40, 18),
    ("LM10013", "Massa acrílica para trincas 400g", "Selante elástico para trincas e rachaduras finas.", "Luxens", "S", 18.50, "un", 10, 4, "AUTONOMO", "PRONTA_ENTREGA", "rachadura trinca fissura parede", 45, 20),
    ("LM10014", "Pincel 2\"", "Pincel para recortes e cantos.", "Luxens", "S", 9.90, "un", 10, 3, "AUTONOMO", "PRONTA_ENTREGA", "pincel pintar canto", 70, 30),

    ("LM11001", "Porcelanato polido Calacata 60 x 60cm (m²)", "Mostruário. Entregue em casa a partir do CD.", "Artens", "S", 69.90, "m²", 11, 1, "APOIO_PARCIAL", "MOSTRUARIO", "porcelanato piso polido branco", 2000, 0),
    ("LM11002", "Porcelanato acetinado Cimento 60 x 60cm (m²)", "Mostruário. Entregue em casa a partir do CD.", "Portobello", "N", 99.90, "m²", 11, 1, "APOIO_PARCIAL", "MOSTRUARIO", "porcelanato piso cinza", 800, 0),
    ("LM11003", "Porcelanato amadeirado 20 x 120cm (m²)", "Mostruário. Entregue em casa a partir do CD.", "Artens", "S", 89.90, "m²", 11, 2, "APOIO_PARCIAL", "MOSTRUARIO", "porcelanato madeira piso", 1200, 0),
    ("LM11004", "Espaçador para piso 2mm (100 un)", "Garante juntas uniformes no assentamento.", "Artens", "S", 6.90, "pacote", 11, 3, "AUTONOMO", "PRONTA_ENTREGA", "espaçador piso junta", 150, 60),
    ("LM11005", "Nivelador de piso (50 un)", "Evita desnível entre peças grandes.", "Cortag", "N", 39.90, "pacote", 11, 3, "AUTONOMO", "PRONTA_ENTREGA", "nivelador piso porcelanato", 60, 25),
    ("LM11006", "Rejunte flexível cinza 1kg", "Para juntas de 1 a 10mm, áreas internas e externas.", "Quartzolit", "N", 17.90, "kg", 11, 4, "AUTONOMO", "PRONTA_ENTREGA", "rejunte piso junta", 100, 45),

    ("LM12001", "Argamassa AC-III 20kg", "Indicada para porcelanato e áreas externas.", "Quartzolit", "N", 42.90, "saco", 12, 1, "AUTONOMO", "PRONTA_ENTREGA", "argamassa porcelanato assentar", 200, 80),
    ("LM12002", "Argamassa AC-III 20kg", "Indicada para porcelanato e áreas externas.", "Standers", "S", 34.90, "saco", 12, 1, "AUTONOMO", "PRONTA_ENTREGA", "argamassa porcelanato assentar", 250, 90),
    ("LM12003", "Cimento CP-II 50kg", "Cimento para uso geral em obra.", "Votoran", "N", 39.90, "saco", 12, 1, "AUTONOMO", "PRONTA_ENTREGA", "cimento obra concreto", 300, 120),
    ("LM12004", "Areia média ensacada 20kg", "Para argamassa de reboco e assentamento.", "Standers", "S", 9.90, "saco", 12, 2, "AUTONOMO", "PRONTA_ENTREGA", "areia obra", 200, 100),
    ("LM12005", "Impermeabilizante flexível 18kg", "Manta líquida para lajes, banheiros e áreas molhadas.", "Vedacit", "N", 159.90, "balde", 12, 2, "APOIO_PARCIAL", "PRONTA_ENTREGA", "impermeabilizar infiltração laje umidade", 30, 12),
]

SIMILARES = [
    ("LM8001", "LM8002"), ("LM8002", "LM8001"),
    ("LM6003", "LM6001"),
    ("LM5002", "LM5003"), ("LM5003", "LM5002"),
    ("LM4002", "LM4001"), ("LM4001", "LM4002"),
    ("LM3002", "LM3001"), ("LM3001", "LM3002"),
    ("LM10003", "LM10001"), ("LM10001", "LM10003"),
    ("LM11002", "LM11001"),
    ("LM7002", "LM7001"), ("LM7001", "LM7002"),
    ("LM7004", "LM7002"),
    ("LM9001", "LM9003"),
    ("LM9011", "LM9004"), ("LM9004", "LM9011"),
    ("LM12001", "LM12002"), ("LM12002", "LM12001"),
]

COMPLEMENTARES = [
    ("LM7001", "LM7003", "Cada prateleira precisa de 2 suportes mão-francesa."),
    ("LM7001", "LM8002", "Para fixar os suportes em parede de alvenaria."),
    ("LM7001", "LM8003", "Parafuso compatível com a bucha S8."),
    ("LM7001", "LM9001", "Bucha S8 pede furo com broca de 8mm."),
    ("LM7001", "LM9005", "Para a prateleira não ficar torta."),
    ("LM7002", "LM7003", "Cada prateleira precisa de 2 suportes mão-francesa."),
    ("LM7003", "LM8002", "Para fixar o suporte em alvenaria."),
    ("LM7003", "LM8003", "Parafuso compatível com a bucha S8."),
    ("LM8001", "LM9001", "Bucha S8 pede furo com broca de 8mm."),
    ("LM8002", "LM9001", "Bucha S8 pede furo com broca de 8mm."),
    ("LM8006", "LM9002", "Bucha S6 pede furo com broca de 6mm."),
    ("LM6001", "LM5002", "Veda a rosca e evita vazamento na instalação."),
    ("LM6001", "LM5004", "Liga o registro da parede à torneira nova."),
    ("LM6002", "LM5002", "Veda a rosca e evita vazamento na instalação."),
    ("LM5001", "LM9008", "Para abrir a torneira e trocar o reparo."),
    ("LM6004", "LM4001", "Isola as emendas dos fios do chuveiro."),
    ("LM6004", "LM5002", "Veda a rosca do cano do chuveiro."),
    ("LM6004", "LM4007", "Confirme que a energia está desligada antes de trocar."),
    ("LM6005", "LM4001", "Isola as emendas dos fios do chuveiro."),
    ("LM6005", "LM5002", "Veda a rosca do cano do chuveiro."),
    ("LM6005", "LM4007", "Confirme que a energia está desligada antes de trocar."),
    ("LM10001", "LM10006", "Rolo para aplicar a tinta."),
    ("LM10001", "LM10007", "Bandeja para o rolo."),
    ("LM10001", "LM10008", "Protege rodapés e batentes."),
    ("LM10001", "LM10012", "Protege piso e móveis."),
    ("LM10002", "LM10006", "Rolo para aplicar a tinta."),
    ("LM10002", "LM10007", "Bandeja para o rolo."),
    ("LM10002", "LM10008", "Protege rodapés e batentes."),
    ("LM10004", "LM10011", "Remova o mofo antes de pintar, senão ele volta."),
    ("LM10004", "LM10006", "Rolo para aplicar a tinta."),
    ("LM10013", "LM10009", "Lixe a área antes de aplicar a massa."),
    ("LM11001", "LM12002", "Porcelanato exige argamassa AC-III."),
    ("LM11001", "LM11004", "Espaçador garante juntas alinhadas."),
    ("LM11001", "LM11006", "Rejunte para finalizar as juntas."),
    ("LM11001", "LM11005", "Nivelador evita degraus entre peças grandes."),
    ("LM11003", "LM12002", "Porcelanato exige argamassa AC-III."),
    ("LM11003", "LM11004", "Espaçador garante juntas alinhadas."),
    ("LM11003", "LM11006", "Rejunte para finalizar as juntas."),
    ("LM12001", "LM9010", "Desempenadeira dentada para espalhar a argamassa."),
    ("LM12002", "LM9010", "Desempenadeira dentada para espalhar a argamassa."),
    ("LM3003", "LM4001", "Isola as emendas dos fios."),
    ("LM3003", "LM8006", "Fixa a base do plafon no teto."),
    ("LM3003", "LM4007", "Confirme que a energia está desligada antes de instalar."),
    ("LM2002", "LM8006", "Fixa os suportes do varão."),
    ("LM2002", "LM9002", "Bucha S6 pede furo com broca de 6mm."),
    ("LM9011", "LM9012", "Pontas para cada tipo de parafuso."),
    ("LM9011", "LM9003", "Brocas para furar madeira, metal e alvenaria."),
    ("LM9011", "LM9016", "Protege os olhos ao furar."),
    ("LM9004", "LM9016", "Protege os olhos ao furar."),
    ("LM9013", "LM9016", "Protege os olhos ao cortar."),
]


def q(texto: str) -> str:
    return "'" + texto.replace("'", "''") + "'"


def montar_mapa():
    nos = []  # (id, código, tipo, x, y)
    arestas = []  # (origem, destino, metros)
    proximo_id = 1

    def novo_no(codigo, tipo, x, y):
        nonlocal proximo_id
        nos.append((proximo_id, codigo, tipo, x, y))
        proximo_id += 1
        return proximo_id - 1

    frente, meio, fundo = [], [], []
    for i in range(len(SECOES)):
        x = CORREDOR_X0 + i * CORREDOR_DX
        frente.append(novo_no(f"F{i + 1:02d}", "PASSAGEM", x, Y_FRENTE))
        meio.append(novo_no(f"C{i + 1:02d}", "CORREDOR", x, Y_MEIO))
        fundo.append(novo_no(f"B{i + 1:02d}", "PASSAGEM", x, Y_FUNDO))

    por_id = {n[0]: n for n in nos}

    def ligar(a, b):
        xa, ya = por_id[a][3], por_id[a][4]
        xb, yb = por_id[b][3], por_id[b][4]
        metros = round((abs(xa - xb) + abs(ya - yb)) / 10, 1)
        arestas.append((a, b, metros))

    for i in range(len(SECOES)):
        ligar(frente[i], meio[i])
        ligar(meio[i], fundo[i])
        if i + 1 < len(SECOES):
            ligar(frente[i], frente[i + 1])
            ligar(fundo[i], fundo[i + 1])

    entrada = novo_no("ENTRADA", "ENTRADA", 499, 610)
    por_id[entrada] = nos[-1]
    ligar(entrada, frente[5])
    ligar(entrada, frente[6])

    atendimento = novo_no("ATENDIMENTO", "ATENDIMENTO", 148, 600)
    por_id[atendimento] = nos[-1]
    ligar(atendimento, frente[1])

    caixa = novo_no("CAIXA", "CAIXA", 850, 600)
    por_id[caixa] = nos[-1]
    ligar(caixa, frente[10])

    return nos, arestas, meio


def gerar() -> str:
    linhas = ["-- Gerado por database/gerador/gerar_seed.py. Não edite à mão.", ""]

    linhas.append("-- Seções")
    for id_secao, nome, cor in SECOES:
        linhas.append(f"INSERT INTO cl_secao (id_secao, nm_secao, ds_cor) VALUES ({id_secao}, {q(nome)}, {q(cor)});")

    nos, arestas, meio = montar_mapa()
    linhas += ["", "-- Vértices do mapa"]
    for id_no, codigo, tipo, x, y in nos:
        linhas.append(
            f"INSERT INTO cl_no_mapa (id_no, cd_no, tp_no, nr_x, nr_y) VALUES ({id_no}, {q(codigo)}, {q(tipo)}, {x}, {y});"
        )
    linhas += ["", "-- Arestas do mapa (metros)"]
    for a, b, metros in arestas:
        linhas.append(
            f"INSERT INTO cl_aresta_mapa (id_no_origem, id_no_destino, nr_distancia_m) VALUES ({a}, {b}, {metros});"
        )

    linhas += ["", "-- Corredores (uma seção por corredor)"]
    for i, (id_secao, _, _) in enumerate(SECOES):
        linhas.append(
            f"INSERT INTO cl_corredor (id_corredor, nr_corredor, id_secao, id_no) VALUES ({i + 1}, {i + 1}, {id_secao}, {meio[i]});"
        )

    linhas += ["", "-- Produtos e estoque"]
    id_por_sku = {}
    for i, p in enumerate(PRODUTOS, start=1):
        sku, nome, desc, marca, mp, preco, un, corr, prat, atend, disp, chaves, qt_sis, qt_gon = p
        id_por_sku[sku] = i
        linhas.append(
            "INSERT INTO cl_produto (id_produto, cd_sku, nm_produto, ds_produto, nm_marca, fl_marca_propria, vl_preco, "
            "ds_unidade, id_corredor, nr_prateleira, tp_atendimento, tp_disponibilidade, ds_palavras_chave) VALUES "
            f"({i}, {q(sku)}, {q(nome)}, {q(desc)}, {q(marca)}, {q(mp)}, {preco:.2f}, {q(un)}, {corr}, {prat}, "
            f"{q(atend)}, {q(disp)}, {q(chaves)});"
        )
        linhas.append(
            f"INSERT INTO cl_estoque (id_produto, qt_sistema, qt_gondola) VALUES ({i}, {qt_sis}, {qt_gon});"
        )

    linhas += ["", "-- Produtos equivalentes"]
    for a, b in SIMILARES:
        linhas.append(
            f"INSERT INTO cl_produto_similar (id_produto, id_similar) VALUES ({id_por_sku[a]}, {id_por_sku[b]});"
        )

    linhas += ["", "-- Produtos complementares (venda complementar)"]
    for a, b, motivo in COMPLEMENTARES:
        linhas.append(
            "INSERT INTO cl_produto_complementar (id_produto, id_complementar, ds_motivo) VALUES "
            f"({id_por_sku[a]}, {id_por_sku[b]}, {q(motivo)});"
        )

    linhas += ["", "COMMIT;", ""]
    return "\n".join(linhas)


if __name__ == "__main__":
    destino = Path(__file__).resolve().parent.parent / "02_seed.sql"
    destino.write_text(gerar(), encoding="utf-8")
    print(f"{destino} ({len(PRODUTOS)} produtos)")
