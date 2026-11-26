# Copiloto Leroy – Assistente de Loja com IA

Challenge FIAP × Leroy Merlin · Sprint 2 · **Grupo 105**

| Integrante | RM |
|---|---|
| Daniel Camilo Gonzalez | 564592 |
| Enzo Fernandes Bertelli | 565482 |
| Danilo Kvet Barbosa | 565735 |

## O problema

Na loja física, o cliente sabe o que quer **resolver** ("minha torneira está pingando"), mas não sabe o que **comprar**,
nem onde cada item está. Nos fins de semana há poucos vendedores, e o "último metro" (achar o produto na prateleira) é
onde a jornada trava. Quando o sistema mostra estoque mas a prateleira está vazia (ruptura), o cliente desiste.

## A solução

Um web app mobile-first que o cliente abre pelo QR Code na entrada da loja:

1. **Conta o problema ou manda uma foto.** A IA (OpenAI) entende, faz perguntas rápidas e monta a lista com quantidades
   corretas. Itens essenciais e opcionais ficam separados, e os complementos ("não esqueça as cunhas niveladoras") aparecem logo abaixo.
   Sem IA disponível, um motor de regras sobre o catálogo responde no lugar.
2. **Recebe o caminho mais curto na loja.** O mapa mostra a rota da entrada até o caixa, passando por cada corredor na melhor ordem.
3. **Prateleira vazia?** Com um toque, o cliente vê um equivalente disponível (marca própria primeiro), pode comprar
   no site ou chamar um vendedor. A ruptura fica registrada para reposição.
4. **Passagem para o vendedor (o diferencial):** o cliente pega uma senha e acompanha a fila em tempo real.
   O vendedor recebe o chamado **com a lista do cliente**, então ninguém precisa explicar tudo de novo.
5. **Painel do vendedor:** fila de atendimento, prateleiras para repor e indicadores da loja.

Também trata os casos levantados no kickoff: produtos **só de mostruário** (porcelanato, entregue em casa), produtos que
**dependem de especialista** (tinta personalizada) e o tipo de atendimento de cada item (autônomo, apoio parcial ou vendedor).

## Arquitetura

```
React (Vite) ──HTTP/JSON──▶ API Java (JAX-RS Jersey + Grizzly) ──JDBC──▶ Oracle (FIAP)
  Vercel                      Render (Docker)          └──HTTPS──▶ OpenAI
```

| Pasta | Conteúdo |
|---|---|
| `api/` | API REST em Java 21 com Maven: recursos JAX-RS, DTOs, DAOs, serviços e algoritmos |
| `web/` | Front-end React (cliente e área do vendedor) |
| `database/` | DDL, carga inicial (87 produtos, 12 corredores, mapa da loja) e o gerador da carga em Python |

## Serviços em nuvem

| Serviço | Papel | Configuração |
|---|---|---|
| Vercel | Hospeda o front-end (pasta `web/`) | `VITE_API_URL` aponta para a API |
| Render | Executa a API em container Docker (`render.yaml`), plano gratuito | `DB_USER`, `DB_PASSWORD`, `OPENAI_API_KEY`, `OPENAI_MODEL`, `CORS_ORIGINS` |
| Oracle FIAP | Banco de dados (tabelas `cl_*`) | Scripts em `database/` |
| OpenAI | Modelo `gpt-5.4-mini`, que entende texto e foto | Chamado só pela API; a chave nunca chega ao navegador |

Senhas e chaves ficam apenas nas variáveis de ambiente de cada serviço. O repositório traz só os modelos
`api/.env.example` e `web/.env.example`, sem valores reais.

## Como a IA é usada

- **O que ela faz:** entende o problema descrito pelo cliente (ou mostrado na foto), faz apenas as perguntas que mudam
  o produto, como a voltagem do chuveiro, e monta a lista com quantidades.
- **O catálogo é a única fonte:** a cada conversa, a API envia à IA os produtos da loja com preço e corredor. Itens
  com SKU fora do catálogo são descartados antes de chegar ao cliente, então a IA não inventa produto nem preço.
- **Escopo restrito:** só assuntos de casa, reforma, manutenção, decoração e jardim. Perguntas fora do tema são
  redirecionadas para os projetos da loja. A IA não promete desconto, prazo, horário nem instalação, e avisa quando a
  loja não vende o que foi pedido.
- **Limites de entrada:** mensagens do cliente de até 500 caracteres, conversa com as 20 mensagens mais recentes e foto
  de até cerca de 4 MB.
- **Privacidade:** a foto vai para a OpenAI apenas para análise naquela conversa e não é gravada no banco. O projeto
  do cliente é anônimo, sem nome, CPF ou telefone.
- **Sem IA, o app continua:** se a OpenAI falhar ou não houver chave, um motor de regras sobre o catálogo
  (`RegrasAssistente`) responde no lugar. A resposta indica a origem no campo `fonte` (`IA` ou `REGRAS`).

## Conteúdos do curso aplicados

| Fase / disciplina | Onde está no projeto |
|---|---|
| POO em Java | Classe abstrata `Produto` com as subclasses `ProdutoProntaEntrega` e `ProdutoMostruario` (polimorfismo em `podeLevarAgora`/`orientacaoCompra`), enums com regras (`StatusChamado.podeAvancarPara`), records, exceções próprias (`ProdutoIndisponivelException`, `RegraNegocioException`) |
| API REST (JAX-RS) | `resource/*`: verbos GET, POST e PUT, status 201/204/404/409/422, DTOs, `ExceptionMapper` e filtro de CORS |
| JDBC / DAO | `dao/*`: `PreparedStatement`, transação com commit/rollback (`ProjetoDAO.inserir`), `SEQUENCE` para a senha do chamado e pool de conexões |
| Banco de dados Oracle | Modelo em 3FN com supertipo/subtipo (produto pronta-entrega × mostruário), autorrelacionamento N:N (similares e complementares), entidade associativa (`cl_item_projeto`), `IDENTITY`, `CHECK`, índices; `IndicadorDAO` usa `JOIN`, `GROUP BY`/`HAVING`, subconsulta e funções de agregação |
| Estruturas de dados e algoritmos | **Grafo ponderado** da loja (lista de adjacência), **Dijkstra** com fila de prioridade, ordem de visita ótima por **programação dinâmica** (Held-Karp), **merge sort** e **busca binária** por SKU no catálogo em memória, fila FIFO de atendimento |
| Testes | JUnit 5 (`AlgoritmosTest`): o Held-Karp é comparado com a força bruta em 50 casos aleatórios |

## Como rodar localmente

Pré-requisitos: Java 21+, Maven, Node 20+ e um Oracle (o da FIAP ou um container local).

```bash
# 1. Banco: rode, em ordem, database/01_ddl.sql e database/02_seed.sql (00_drop.sql limpa tudo)

# 2. API (http://localhost:8080)
cd api
mvn package
DB_USER=rm000000 DB_PASSWORD=senha OPENAI_API_KEY=sk-... java -jar target/copiloto-leroy-api.jar

# 3. Front-end (http://localhost:5173)
cd web
npm install
VITE_API_URL=http://localhost:8080 npm run dev
```

As variáveis de ambiente estão em `api/.env.example` e `web/.env.example`. Sem `OPENAI_API_KEY`, o assistente funciona
em modo de regras.

Oracle local para desenvolvimento:

```bash
docker run -d --name copiloto-oracle -p 1522:1521 -e ORACLE_PASSWORD=oracle \
  -e APP_USER=copiloto -e APP_USER_PASSWORD=copiloto gvenzl/oracle-free:slim-faststart
# DB_URL=jdbc:oracle:thin:@//localhost:1522/FREEPDB1
```

## Endpoints

| Método | Caminho | Descrição |
|---|---|---|
| GET | `/` | Saúde (banco, IA, produtos carregados) |
| GET | `/produtos?busca=` | Busca por nome, marca, palavra-chave ou SKU |
| GET | `/produtos/{sku}/localizacao` | Rota até o produto (409 com alternativas se a prateleira estiver vazia) |
| PUT | `/produtos/{sku}/reposicao` | Vendedor confirma a reposição da prateleira |
| GET | `/mapa` | Planta da loja (nós, arestas, corredores) |
| POST | `/assistente/conversa` | Conversa com a IA (texto e foto opcional) e lista sugerida |
| POST | `/projetos` · GET `/projetos/{id}` · GET `/projetos/{id}/rota` | Lista do cliente e rota otimizada |
| POST | `/rupturas` · PUT `/rupturas/{id}/desfecho` | Registro de prateleira vazia e do que o cliente fez |
| POST/GET | `/chamados` · GET `/chamados/{id}` | Abrir chamado e consultar a fila |
| PUT | `/chamados/{id}/aceite` · `/conclusao` · `/cancelamento` | Ciclo de vida do atendimento |
| GET | `/indicadores` | Indicadores do painel do vendedor |

## Deploy

| | Link |
|---|---|
| App (cliente) | https://copiloto-leroy-grupo105.vercel.app |
| Área do vendedor | https://copiloto-leroy-grupo105.vercel.app/vendedor |
| API | https://copiloto-leroy-api.onrender.com |

A API usa o plano gratuito do Render, que desliga o serviço após cerca de 15 minutos sem uso; o primeiro acesso
depois disso pode levar até 1 minuto. A configuração de cada serviço está em [Serviços em nuvem](#serviços-em-nuvem).

Os dados de estoque são simulados, conforme a orientação do kickoff.
