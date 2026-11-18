# Registro de uso de inteligência artificial generativa

Este arquivo registra o uso de IA no desenvolvimento da entrega de **Arquitetura Orientada a Serviços (SOA) e Web Services** da Global Solution, conforme exigido pela atividade.

**Ferramenta:** Claude (Anthropic), via Claude Code.

A IA foi usada para gerar o código da aplicação a partir das decisões de produto e de arquitetura tomadas pela equipe. Os prompts abaixo são reconstruções fiéis das instruções dadas ao longo do trabalho, e não transcrições literais. As verificações descritas em cada etapa foram efetivamente executadas, e os artefatos que as comprovam estão em `docs/evidencias/`.

---

## 1. Definição da arquitetura e do modelo de domínio

### Prompt utilizado

> A partir da descrição do projeto S.B.P. — totem físico na parada de ônibus, aplicativo mobile e site, ligados por QR Code — proponha o modelo de domínio do backend: entidades, relacionamentos e as regras de negócio que pertencem a cada entidade. O projeto atende o cidadão que espera no ponto sem saber quanto tempo falta, com foco em acessibilidade para pessoas com deficiência visual.

### Finalidade

Transformar a proposta do produto em um modelo de domínio implementável, cobrindo os itens avaliados na disciplina: estruturação do domínio, persistência, migrações, documentação automática e testes.

### Decisão da equipe

A equipe escolheu a stack entre três alternativas apresentadas: **Java 21 com Spring Boot e PostgreSQL em Docker**, em vez de Spring Boot com Oracle da FIAP ou Python com FastAPI. O critério foi não depender de credenciais e de VPN no momento da correção, e manter coerência com o que o roteiro do pitch já afirmava sobre o backend.

A equipe também definiu que o repositório seria `GS-SOA-2-26`, contendo apenas o backend da aplicação.

### Como a resposta foi verificada na prática

O modelo proposto foi conferido contra os requisitos do enunciado antes da implementação. Depois de implementado, a aderência entre o mapeamento das entidades e o schema criado pelas migrações foi verificada pelo próprio Hibernate, configurado com `ddl-auto: validate` — nessa configuração, qualquer divergência entre entidade e tabela impede a aplicação de iniciar. A aplicação subiu normalmente contra o PostgreSQL 16, o que comprova que as 9 tabelas e suas colunas correspondem ao modelo.

---

## 2. Implementação das camadas da aplicação

### Prompt utilizado

> Gere o projeto Spring Boot organizado em camadas (domain, service, web, exception, config), com nomes e mensagens em português, Bean Validation nos DTOs de entrada e um tratador global de erros que padronize todas as respostas de falha da API.

### Finalidade

Obter uma implementação consistente das camadas, evitando que cada controlador trate erros de forma diferente e que as entidades JPA vazassem para fora da aplicação.

### Como a resposta foi verificada na prática

As respostas da API foram exercitadas com a aplicação em execução, e as saídas estão gravadas em `docs/evidencias/`. Foram conferidos, em particular:

- o formato padronizado de erro em três situações distintas — recurso inexistente (HTTP 404, evidência `13`), regra de negócio violada (HTTP 409, evidência `14`) e entrada inválida (HTTP 400, evidência `15`);
- a validação de entrada apontando **cada** campo inválido individualmente, e não apenas o primeiro: a evidência `15` mostra os quatro campos do corpo enviado reportados de uma vez;
- a geração da documentação OpenAPI a partir do próprio código, salva em `docs/evidencias/16-openapi.json`.

---

## 3. Migrações e carga de dados reais

### Prompt utilizado

> Escreva as migrações Flyway para PostgreSQL e gere a carga inicial a partir do GTFS oficial da SPTrans que está no repositório, usando linhas, paradas e itinerários reais de São Paulo. Os cadastros de cidadãos devem ser fictícios, conforme os combinados da Global Solution.

### Finalidade

Separar a criação do schema da carga de dados e popular a base com dado público real, de modo que o itinerário exibido pelo totem corresponda ao trajeto real da linha.

### Problema encontrado e correção

A primeira versão da carga associava linhas a paradas que elas **não atendem** — por exemplo, a linha 1177-10 à parada 3609008, que pertence ao itinerário da 1177-31. Os pares foram extraídos novamente do `stop_times.txt` do GTFS e conferidos um a um antes da carga, de modo que toda previsão e toda rotina do seed liguem uma linha a uma parada que ela realmente percorre.

### Como a resposta foi verificada na prática

Com o banco no ar, foram consultadas a tabela de controle do Flyway e o conteúdo carregado. As saídas estão em `docs/evidencias/00-migracoes-flyway.txt` e `00-dados-carregados.txt`, e confirmam as migrações aplicadas com sucesso e o volume carregado: 12 linhas, 276 paradas, 282 itinerários, 23 pontos de interesse e 3 cidadãos fictícios. Nesta etapa existiam quatro migrações; a `V5` e a `V6` vieram das etapas 5 e 6 descritas abaixo.

---

## 4. Testes automatizados

### Prompt utilizado

> Escreva testes para as regras que o cidadão vê na tela: o cálculo de distância por Haversine, o carrossel de emergência abaixo de 3 minutos, o texto falado pelo botão tátil, a detecção de totem que parou de sincronizar e a validação de entrada dos endpoints. Use Mockito nos serviços e MockMvc na camada web, de modo que a suíte não dependa de banco nem de Docker.

### Finalidade

Cobrir as regras de negócio que determinam o que aparece no totem, em vez de perseguir cobertura sobre código trivial.

### Como a resposta foi verificada na prática

A suíte foi escrita e executada, e o resultado está em `docs/evidencias/00-testes.txt`: **60 testes, 0 falhas e 0 erros**, assim distribuídos:

| Classe | Testes | O que cobre |
| :--- | ---: | :--- |
| `DominioTest` | 29 | As regras dentro das entidades: Haversine, carrossel de emergência, texto do botão tátil, faixa de espera, saúde e acessibilidade do totem, reconhecimento de linha noturna |
| `ParadaServiceTest` | 8 | Código GTFS repetido, parada com totem instalado, limite de raio, ordenação por distância, tempo a pé |
| `TotemServiceTest` | 7 | Montagem do painel, batimento do totem, regras de instalação, geração do token do QR Code |
| `PrevisaoServiceTest` | 7 | Integridade da faixa de espera e limite de previsão útil |
| `ParadaControllerTest` | 8 | Camada HTTP: códigos de resposta, validação campo a campo e formato padronizado de erro |
| `SbpApiApplicationTests` | 1 | O contexto do Spring sobe por inteiro |

Os testes rodam sobre H2 em memória e **não dependem do Docker**, de modo que a verificação pode ser repetida em qualquer máquina com `./mvnw test`. A suíte foi executada novamente a cada mudança de domínio: ao acrescentar a faixa de espera, por exemplo, passou de 50 para 60 testes e continuou verde.

---

## 5. Correções a partir da execução da aplicação

### Prompt utilizado

> Suba a aplicação, chame os endpoints principais e verifique se o painel do totem realmente entrega o que foi projetado.

### Finalidade

Conferir o comportamento real da API, e não apenas o resultado dos testes, já que o painel do totem é o endpoint central da entrega.

### Problemas encontrados e correções

A execução revelou dois defeitos que os testes não alcançariam:

1. **O painel abria sem previsões.** As previsões do seed são gravadas com a data da migração e expiram em 5 minutos por regra de negócio, de modo que o painel ficava vazio em qualquer execução posterior. O script de evidências passou a registrar previsões recentes pela própria API antes de capturar o painel, que é o mesmo caminho que a integração com a API Olho Vivo seguirá em produção.
2. **O painel abria sem referências urbanas.** Os três totens do piloto estavam em paradas periféricas, enquanto os pontos de interesse carregados são equipamentos do centro de São Paulo, a mais de 20 km. A migração `V5` reposicionou o piloto, deixando um totem ativo na Av. Prestes Maia, a 222 metros da Estação da Luz, e criando um quarto totem em manutenção para manter demonstrável a regra que bloqueia o painel de um equipamento fora de operação.

### Como a resposta foi verificada na prática

Após a correção, o painel do totem (`docs/evidencias/03-painel-totem.txt`) passou a retornar as quatro previsões ordenadas por tempo de chegada, nas três origens possíveis de dado, com o carrossel de emergência destacando o veículo que já está no ponto, a referência urbana a 222 metros e o texto completo que o botão tátil lê em voz alta.

---

## 6. Faixa de espera nas previsões

### Prompt utilizado

> O modelo de ML passou a publicar uma faixa de espera calibrada, além do número central. Leve isso para dentro da API: a previsão de origem MODELO_ML deve carregar a faixa, o totem deve anunciá-la em voz alta, e o banco não pode aceitar faixa incoerente.

### Finalidade

Impedir que o totem anuncie uma precisão que o modelo não tem. Com erro médio de 7,6 minutos, anunciar "24 minutos" induz o passageiro ao erro; anunciar "entre 13 e 37 minutos" preserva a utilidade da informação para decidir entre esperar e procurar outro caminho.

### Decisão da equipe

A faixa é **opcional**, e não obrigatória para toda previsão: telemetria e quadro publicado são pontuais por natureza, e exigir faixa deles seria atribuir incerteza onde ela não existe. Apenas a origem `MODELO_ML` a utiliza.

### Como a resposta foi verificada na prática

A regra foi protegida em duas camadas, e as duas foram exercitadas:

- **No banco**, por três `CHECK` constraints da migração `V6`: ou a faixa vem completa ou não vem; não pode estar invertida; e precisa conter a própria estimativa central.
- **No serviço**, com mensagens em português que explicam o problema ao integrador.

Foram escritos 10 testes novos (3 de domínio e 7 de serviço), e a suíte passou de 50 para **60 testes, 0 falhas**. Com a aplicação no ar, foi confirmado que o painel do totem exibe `"13 a 37 min"` apenas na previsão de origem `MODELO_ML`, e que uma faixa incoerente é recusada com HTTP 409:

```
A estimativa de 40 minutos está fora da própria faixa (8 a 25 min).
```

---

## 7. Acentuação do texto que a API publica

### Prompt utilizado

> O Swagger está mostrando "Cidadaos", "Previsoes" e "onibus" sem acento, enquanto a descrição do topo está acentuada. Corrija o texto que a API publica.

### Finalidade

A descrição principal da API, escrita à mão no `OpenApiConfig`, estava acentuada, mas as 277 linhas de `@Tag`, `@Operation`, `@Schema` e `@ApiResponse` do pacote `web` tinham sido escritas sem acento. O contraste aparecia na mesma tela do Swagger UI, e o problema atingia também o texto que o botão tátil do totem lê em voz alta, onde a falta de acento altera a pronúncia do sintetizador.

### Problemas encontrados e correções

- A primeira tentativa usou uma expressão regular para localizar as strings. Ela casava também o trecho **entre** duas strings concatenadas e renomeava código: `previsao.textoAcessivel()`, `.codigo(...)` e a variável `maximo`. Foi descartada antes de ser aplicada.
- A versão usada varre o arquivo caractere a caractere, distinguindo comentário, literal de char, `text block` e string comum, e altera apenas o conteúdo das strings.
- Ficaram protegidos deliberadamente: as consultas JPQL (onde `situacao` e `codigo` são nomes de campo), os caminhos de rota (`/api/cidadaos/{id}`), os nomes de coluna do `@Column` — que quebrariam a validação do Flyway — e as constantes de enum como `MANUTENCAO`.
- Duas strings de uma palavra só exigiram correção manual, porque a regra automática protege literais sem espaço: o nome da tag `Previsões` e o rótulo `Cidadão` da exceção de 404.

### Como a resposta foi verificada na prática

A suíte continuou em **60 testes, 0 falhas**, o que é justamente o que comprova a consistência: os testes comparam o texto acessível com a string esperada, de modo que produção e teste precisavam mudar juntos. Com a imagem reconstruída, foi conferido no Swagger UI que as seis tags aparecem acentuadas e que o 404 responde `"Recurso não encontrado"`. Os 20 arquivos de evidência foram gerados de novo.

### A segunda rodada, a partir do Swagger

Com a aplicação no ar, uma auditoria da especificação OpenAPI comparou cada exemplo com a resposta real da API e com as constantes do código. Encontrou mais dez textos errados, sendo o mais grave `"Este horário e uma estimativa"` — a frase que o sintetizador de voz lê para quem não enxerga a tela, onde o acento altera a pronúncia. Tinha escapado porque o dicionário da primeira rodada ignorava palavras de duas letras, que é onde está a diferença entre o "e" de ligação e o "é" do verbo. A auditoria também confirmou que nenhum exemplo está fora do enum ou dos limites declarados, e que os números citados nas descrições (janela de 30 min, máximo de 10 rotinas, raio de 5 km, descarte em 5 min) correspondem às constantes.

### Os dados das migrações

As referências urbanas e os 23 pontos de interesse escritos à mão nas migrações `V4` e `V5` também estavam sem acento, e entravam no texto falado: o totem lia `"proximo a Estacao da Luz"`. Foram 31 correções. Como editar migração já aplicada muda o checksum, o volume do banco foi recriado e as seis migrações rodaram do zero — o que serviu de teste adicional: elas sobem limpas num banco vazio, e as contagens voltaram idênticas (12 linhas, 276 paradas, 282 itinerários, 4 totens). O texto que o totem fala sai agora acentuado de ponta a ponta:

```
Parada Av. Prestes Maia, 940, Av. Prestes Maia, próximo à Estação da Luz.
O ônibus da linha 1177-10 deve chegar entre 4 e 19 minutos. Este horário é uma estimativa.
```
