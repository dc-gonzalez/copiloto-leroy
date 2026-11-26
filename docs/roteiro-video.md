# Roteiro do vídeo pitch

Copiloto Leroy – Assistente de Loja com IA · duração final 1:56 (limite de 3 min)

Gravação automática do app real (Playwright) com dois celulares lado a lado, cliente e vendedor, e a
legenda de cada passo à esquerda. Narração gerada no ElevenLabs (voz Paulo Radames, modelo
`eleven_multilingual_v2`). Na síntese de voz, "Leroy" é enviado como "Lerói" para a pronúncia em
português; na tela continua "Leroy".

| Tempo | Passo | Tela | Narração |
|---|---|---|---|
| 0:00–0:22 | O problema: A loja enche, o vendedor não dá conta | Início do app | "Sábado de manhã, a loja está cheia. O cliente sabe o problema que tem em casa, mas não sabe o que comprar. Não acha o produto na gôndola e, quando chega ao corredor, a prateleira pode estar vazia. E os vendedores não dão conta de todo mundo. O Copiloto Leroy é um assistente de loja com inteligência artificial que leva o cliente do problema até o caixa, e o vendedor até quem precisa dele." |
| 0:22–0:34 | 1 · Conversa: O cliente fala do jeito dele | Digitar "Preciso trocar o chuveiro", escolher 220V | "O cliente descreve o problema com as próprias palavras. O assistente faz só a pergunta que muda o produto, aqui a voltagem, e responde em linguagem simples. Também dá para mandar uma foto do problema." |
| 0:34–0:49 | 2 · Lista do projeto: Tudo o que precisa, com quantidade e preço | Lista do projeto: adicionar um complemento, ver o total | "Em seguida, ele monta a lista com o produto certo, o material de instalação e o que costuma ser esquecido, sempre com produtos e preços do catálogo da loja. O cliente desmarca o que já tem em casa e vê o total antes de andar pela loja." |
| 0:49–1:01 | 3 · Caminho na loja: A melhor ordem entre os corredores | Mapa, resumo da rota e primeira parada: "Peguei" | "O app calcula a melhor ordem de visita entre os corredores, com programação dinâmica, e mostra quantos metros isso economiza. Cada parada indica o corredor e a prateleira, e o cliente marca o que já pegou." |
| 1:01–1:12 | 4 · Prateleira vazia: Sem perder a venda na ruptura | "Não encontrei" → equivalente | "Se a prateleira está vazia, o app oferece um equivalente, de preferência da marca própria, a compra pelo site, ou um vendedor. Cada ocorrência fica registrada para a reposição." |
| 1:12–1:26 | 5 · Atendimento: O vendedor chega sabendo o que fazer | "Chamar vendedor" → senha; fila do vendedor ao lado | "O cliente quer ser atendido por uma pessoa. Por isso, o foco é deixar o vendedor mais rápido. O cliente pega uma senha e continua comprando. O vendedor vê a fila, o corredor e a lista do cliente, sem precisar ouvir tudo de novo." |
| 1:26–1:33 | 5 · Atendimento: O cliente acompanha em tempo real | Vendedor aceita; cliente vê quem está indo | "Quando o vendedor aceita, o cliente vê na hora quem está indo até ele, e pode continuar as compras enquanto espera." |
| 1:33–1:40 | 6 · Gestão da loja: Indicadores para a equipe | Aba Indicadores: repor prateleira | "No painel, a loja acompanha o tempo de espera, os chamados por seção e as prateleiras para repor, com um toque." |
| 1:40–1:56 | Copiloto Leroy: O cliente encontra, o vendedor atende quem precisa | Volta ao início do app | "Copiloto Leroy: o cliente encontra tudo o que precisa, e o vendedor atende quem mais precisa dele. Feito com Java, Oracle, React e inteligência artificial, publicado e pronto para testar. Obrigado." |

## Antes de gravar de novo

- Zerar os dados de teste do banco (`00_drop.sql`, `01_ddl.sql`, `02_seed.sql`) para a senha começar em 001.
- Depois da gravação, zerar o banco outra vez e fazer um novo deploy da API no Render, que guarda o
  catálogo e o estoque em memória.
