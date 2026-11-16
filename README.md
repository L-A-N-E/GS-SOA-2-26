# S.B.P. — Smart Bus Point · Arquitetura Orientada a Serviços (SOA) e Web Services

**Global Solution 2026.2 — Desafio CitizenTech**
FIAP · Engenharia de Software · 3º ano de Fev

> Backend da CitizenTech S.B.P.: a API que entrega ao totem da calçada, ao aplicativo e ao site a mesma informação sobre o ônibus — inclusive para quem não tem celular, bateria ou dados móveis.

---

## Ficha da CitizenTech

| Campo | Conteúdo |
| :--- | :--- |
| **Nome da CitizenTech** | S.B.P. — Smart Bus Point |
| **Cidade e bairro** | São Paulo (SP) — rede municipal de ônibus da SPTrans |
| **Cidadão beneficiado** | Passageiro que depende do ônibus e espera no ponto sem saber quanto tempo falta: trabalhador, estudante, idoso e pessoa com deficiência visual |
| **Problema, em uma frase** | O cidadão que espera no ponto não sabe quanto tempo ainda vai esperar, e essa incerteza o afasta do transporte público. |
| **Como funciona** | Um totem na calçada, um aplicativo e um site mostram a previsão de chegada, o mapa do trajeto e as referências urbanas do bairro. O QR Code do totem liga o ponto físico ao celular. |
| **O que esta disciplina faz** | É o backend que sustenta os três: domínio, banco de dados, API REST documentada e as regras que decidem o que pode ser exibido ao cidadão |
| **Indicador de impacto** | Nº de paradas com informação em tempo real no ponto e nº de totens plenamente acessíveis (voz + Braille + parada adaptada) |
| **ODS conectados** | ODS 9, ODS 10, ODS 11 |
| **Integrantes** | Alice Santos Bulhões (RM554499) · Eduardo Oliveira Cardoso Madid (RM556349) · Guilherme da Cunha Melo (RM555310) · Nicolas Haubricht Hainfellner (RM556259) |

---

## O que está neste repositório

```
GS-SOA-2-26/
├── README.md                    este arquivo
├── prompts.md                   registro do uso de IA
├── docker-compose.yml           PostgreSQL 16 e, sob profile, a própria API
├── Dockerfile                   imagem da aplicação
├── docs/
│   ├── EVIDENCIAS.md            o que cada evidência comprova
│   ├── gerar-evidencias.sh      script que captura as respostas da API
│   └── evidencias/              20 capturas: API, banco, migrações e testes
└── src/
    ├── main/java/br/com/fiap/sbp/    domínio, serviços e camada web
    ├── main/resources/db/migration/  as 6 migrações do Flyway
    └── test/java/                    os 60 testes automatizados
```

## Tecnologias

| Camada | Tecnologia |
| :--- | :--- |
| Linguagem | Java 21 |
| Framework | Spring Boot 3.5.3 (Web, Data JPA, Validation, Actuator) |
| Banco de dados | PostgreSQL 16 |
| Migrações | Flyway |
| Documentação | springdoc-openapi 2.8.6 (OpenAPI 3 + Swagger UI) |
| Testes | JUnit 5, Mockito, AssertJ, MockMvc, H2 |
| Build | Maven (com wrapper — não é preciso instalar o Maven) |

## Como executar

**Pré-requisitos:** Java 21 e Docker.

**1. Suba o banco de dados**

```bash
docker compose up -d
```

**2. Rode a aplicação**

```bash
./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd spring-boot:run`. Na primeira execução o Flyway cria o schema e carrega os dados reais da SPTrans automaticamente.

> Se o start falhar com `Unable to establish loopback connection`, siga pelo caminho em container mais abaixo. É uma limitação do JVM em algumas máquinas Windows, não da aplicação.

**3. Abra a documentação**

| Recurso | Endereço |
| :--- | :--- |
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| Especificação OpenAPI | http://localhost:8080/v3/api-docs |
| Saúde da aplicação | http://localhost:8080/actuator/health |
| Situação das migrações | http://localhost:8080/actuator/flyway |

**4. Rode os testes**

```bash
./mvnw test
```

Os testes usam H2 em memória e **não precisam do Docker**.

### Alternativa: tudo em container

Um `Dockerfile` sobe a API junto com o banco, no lugar dos passos 1 e 2:

```bash
docker compose --profile app up -d --build
```

A aplicação fica no mesmo endereço, `http://localhost:8080`. O serviço está atrás de um *profile*, então o `docker compose up -d` do passo 1 continua subindo apenas o PostgreSQL. Dentro do container Linux a falha de loopback do Windows não ocorre.

### Configuração por variáveis de ambiente

| Variável | Padrão | Para que serve |
| :--- | :--- | :--- |
| `SBP_DB_URL` | `jdbc:postgresql://localhost:5432/sbp` | Endereço do banco |
| `SBP_DB_USER` / `SBP_DB_PASSWORD` | `sbp` / `sbp` | Credenciais |
| `SBP_PORT` | `8080` | Porta da aplicação |
| `SBP_QRCODE_BASE_URL` | `https://sbp.mobi/p/` | Base do endereço que o QR Code abre |

## Estruturação do domínio

O código é organizado em camadas, com uma responsabilidade clara em cada uma:

```
br.com.fiap.sbp
├── domain
│   ├── entity/       as 8 entidades JPA, com as regras que pertencem a elas
│   ├── enums/        7 enums de domínio, com comportamento próprio
│   └── repository/   acesso a dados (Spring Data JPA + JPQL)
├── service/          regras de negócio e orquestração, com @Transactional
├── web
│   ├── controller/   6 controladores REST, só HTTP
│   ├── dto/          records de entrada e saída, com Bean Validation
│   └── mapper/       conversão entidade ↔ DTO em um ponto único
├── exception/        exceções de domínio e o tratador global de erros
└── config/           configuração do OpenAPI
```

### O modelo

```
Cidadao ──1:N──> RotinaProgramada ──N:1──> Linha
                        │  │                 │
                        │  └──N:1──> Parada  │ N:M (via Itinerario, com ordem)
                        │                    │
Parada <──1:1── Totem   Parada <────────────-┘
   │
   └──1:N──> PrevisaoChegada <──N:1── Linha

PontoDeInteresse (ligado às paradas por proximidade geográfica)
```

| Entidade | O que representa |
| :--- | :--- |
| `Linha` | Linha de ônibus. Reconhece as linhas noturnas do Corujão pelo prefixo `N` |
| `Parada` | Ponto de parada, com a referência urbana que o cidadão reconhece e o cálculo de distância por Haversine |
| `Itinerario` | A sequência ordenada de paradas de uma linha — o mapa de rota do totem |
| `Totem` | O equipamento na calçada, seu token de QR Code e sua saúde operacional |
| `PrevisaoChegada` | A resposta à pergunta do cidadão, com a **origem** do dado e o texto falado pelo botão tátil |
| `PontoDeInteresse` | Hospitais, shoppings, estações e teatros exibidos no mapa |
| `Cidadao` | Cadastro do passageiro e suas preferências de acessibilidade |
| `RotinaProgramada` | O trajeto habitual que alimenta o card Rota de Hoje |

**As regras moram nas entidades, não espalhadas nos serviços.** Alguns exemplos:

- `PrevisaoChegada.emergencia()` — faltam menos de 3 minutos, então a linha ocupa o card principal do carrossel do totem;
- `PrevisaoChegada.textoAcessivel()` — monta a frase que o sintetizador de voz lê, por extenso e avisando quando o horário é uma estimativa;
- `Totem.precisaAtencao()` — um totem marcado como ativo que parou de sincronizar continua ligado na rua mostrando informação velha, e precisa ser detectado;
- `Totem.plenamenteAcessivel()` — só vale como acessível o totem com voz, Braille **e** parada adaptada para cadeirantes;
- `Parada.distanciaKm()` — Haversine, para responder "qual é o ponto mais perto de mim".

### Uma decisão que vale explicar

`PrevisaoChegada` guarda a **origem** do dado: telemetria em tempo real, quadro de horários publicado ou estimativa de um modelo preditivo. O cidadão tem o direito de saber se está vendo onde o ônibus está ou onde ele deveria estar — e o totem anuncia isso em voz alta quando a informação é uma estimativa.

Mais do que isso: quando a previsão vem do modelo, ela carrega uma **faixa** em vez de um número. O modelo erra cerca de 7,6 minutos, então anunciar "24 minutos" prometeria uma precisão que ele não tem. A faixa é calibrada por predição conformal para cobrir 80% dos casos, e a cobertura medida foi de 79,8%. O botão tátil passa a dizer *"deve chegar entre 13 e 37 minutos"*. Previsões de telemetria continuam pontuais, porque são exatas por natureza.

O banco e o serviço protegem essa faixa: meia faixa, faixa invertida ou faixa que não contém a própria estimativa são recusadas, tanto por `CHECK` constraints quanto pela camada de negócio.

## Conexão com banco de dados

PostgreSQL 16, acessado por Spring Data JPA com pool HikariCP.

O **Flyway é o dono do schema** e o Hibernate roda com `ddl-auto: validate`: ele apenas confere se o mapeamento das entidades bate com o que as migrações criaram. Se divergirem, a aplicação **não sobe** — o erro aparece no start, e não em produção.

As consultas de proximidade (`/api/paradas/proximas`) primeiro filtram por uma **caixa de coordenadas** no banco, aproveitando o índice composto `(latitude, longitude)`, e só depois calculam a distância exata em memória. Sem isso, cada consulta do aplicativo percorreria as 22 mil paradas da cidade.

## Migrações

```
src/main/resources/db/migration/
├── V1__criar_schema_inicial.sql              9 tabelas, chaves e constraints de domínio
├── V2__criar_indices.sql                     índices, cada um justificado por uma consulta real
├── V3__adicionar_classificacao_ml_na_parada.sql   classificação das paradas por oferta e espera
├── V4__carregar_dados_sptrans.sql            carga de dados reais da SPTrans
├── V5__ajustar_piloto_dos_totens.sql         correção do piloto, feita ao exercitar a API
└── V6__adicionar_faixa_de_espera.sql         faixa de 80% nas previsões   
```

`V3` incorpora ao domínio o resultado de uma análise prévia das 21.933 paradas de São Paulo, que as agrupou por oferta e espera e produziu uma fila de instalação dos totens. A classificação entra como as colunas `grupo_ml` e `prioridade_totem` da parada, e é a partir dela que a API responde quais paradas são prioritárias.

`V4` carrega **dados reais**: 12 linhas, 276 paradas e seus itinerários, extraídos do GTFS oficial da SPTrans (https://www.sptrans.com.br/desenvolvedores/), válido de 01/10/2023 a 01/06/2027. Os cadastros de cidadãos ao final são **fictícios** — conforme os combinados da GS, nenhum dado pessoal real é utilizado.

## Documentação automática dos endpoints

Gerada pelo springdoc a partir do próprio código: as anotações nos controladores e nos DTOs viram a especificação OpenAPI 3. Assim a documentação não envelhece em relação à implementação, e fica em **http://localhost:8080/swagger-ui.html**.

### Os principais endpoints

Onze dos 43 endpoints da API — os que sustentam as telas do totem, do aplicativo e do site:

| Método | Caminho | O que faz |
| :--- | :--- | :--- |
| `GET` | `/api/totens/{codigo}/painel` | **A tela inteira do totem em uma chamada** |
| `GET` | `/api/totens/qrcode/{token}` | O mesmo painel, a partir do QR Code lido no ponto |
| `GET` | `/api/totens/atencao` | Totens que pararam de sincronizar |
| `POST` | `/api/totens/{codigo}/sincronizar` | Batimento enviado pelo próprio equipamento |
| `GET` | `/api/paradas/proximas` | Qual é o ponto mais perto de mim |
| `GET` | `/api/paradas/linha/{id}` | Trajeto da linha, na ordem das paradas |
| `GET` | `/api/linhas` | Busca unificada: número da linha ou nome do trajeto |
| `GET` | `/api/previsoes/parada/{id}` | Previsões vigentes de uma parada |
| `POST` | `/api/previsoes` | Recebe previsão da telemetria ou do modelo de ML |
| `GET` | `/api/cidadaos/{id}/rota-de-hoje` | O card Rota de Hoje do aplicativo |
| `GET` | `/api/pontos-interesse/parada/{id}` | Referências urbanas próximas da parada |

Todos os erros respondem no mesmo formato, tratado por um `@RestControllerAdvice`:

```json
{
  "momento": "2026-11-19T21:35:12",
  "status": 404,
  "erro": "Recurso não encontrado",
  "mensagem": "Parada não encontrado(a) para o identificador: 999",
  "caminho": "/api/paradas/999"
}
```

Erros de validação acrescentam o campo `camposInvalidos`, apontando cada campo e o motivo.

### Experimentando em 30 segundos

```bash
curl http://localhost:8080/api/totens/SBP-0003/painel
```

```bash
curl "http://localhost:8080/api/paradas/proximas?latitude=-23.4319&longitude=-46.7871&raioKm=1"
```

```bash
curl http://localhost:8080/api/totens/qrcode/sbp-c3d4e5f6a1b2
```

O totem `SBP-0004` está propositalmente em manutenção na carga inicial, para demonstrar a regra que impede um equipamento fora de operação de exibir informação (HTTP 409).

> As previsões expiram em 5 minutos por regra de negócio. Se o painel vier com `"previsoes": []`, registre previsões novas com `bash docs/gerar-evidencias.sh`, que as envia pela própria API antes de capturar as respostas.

## Testes automatizados

```bash
./mvnw test
```

| Classe | O que cobre |
| :--- | :--- |
| `DominioTest` | As regras dentro das entidades: Haversine, carrossel de emergência, texto falado pelo botão tátil, saúde do totem, acessibilidade plena |
| `ParadaServiceTest` | Regras de negócio com repositórios simulados (Mockito): código repetido, parada com totem, limite de raio, ordenação por distância |
| `TotemServiceTest` | A montagem do painel, o batimento do totem e as regras de instalação |
| `PrevisaoServiceTest` | A faixa de espera: recusa de faixa incompleta, invertida ou que não contém a estimativa |
| `ParadaControllerTest` | A camada HTTP (`@WebMvcTest` + MockMvc): códigos de resposta, validação de entrada e o formato padronizado de erro |
| `SbpApiApplicationTests` | O contexto do Spring sobe por inteiro |

**60 testes, 0 falhas e 0 erros.** A contagem por classe está em [`docs/evidencias/00-testes.txt`](docs/evidencias/00-testes.txt).

Os testes rodam sobre **H2 em memória**, para que a suíte execute em qualquer máquina sem depender do Docker. As migrações são escritas para PostgreSQL e validadas contra o banco real quando a aplicação sobe com `docker compose up -d`.

## Evidências

[`docs/evidencias/`](docs/evidencias/) tem 25 evidências da aplicação no ar: 16 respostas da API, cada uma com o método, o caminho, o JSON e o código HTTP; 4 consultas feitas direto no banco e na suíte de testes; e 5 prints de tela do Swagger UI, do Flyway, dos testes e do banco. As quatro que mais mostram o projeto funcionando:

| Evidência | O que comprova |
| :--- | :--- |
| [`03-painel-totem.txt`](docs/evidencias/03-painel-totem.txt) | A tela inteira do totem em uma chamada, com o grupo de ML da parada, a faixa de espera e o texto falado |
| [`00-migracoes-flyway.txt`](docs/evidencias/00-migracoes-flyway.txt) | As seis migrações aplicadas, consultadas direto no PostgreSQL |
| [`00-testes.txt`](docs/evidencias/00-testes.txt) | Os 60 testes, detalhados por classe |
| [`swagger-ui.png`](docs/evidencias/swagger-ui.png) | A documentação OpenAPI 3.1 gerada pelo springdoc a partir do código, com as seis áreas da API |

A lista completa e o passo a passo para gerar tudo de novo estão em [`docs/EVIDENCIAS.md`](docs/EVIDENCIAS.md).

## Uso de IA

Conforme os combinados da GS, o uso de IA como copiloto está registrado em [`prompts.md`](prompts.md).
