# Evidências da aplicação

Esta pasta reúne as provas de que a API do S.B.P. funciona: as respostas reais dos endpoints, as migrações aplicadas e a suíte de testes.

## Como gerar as evidências

**1. Suba o banco**

```bash
docker compose up -d
```

**2. Suba a aplicação** (em outro terminal)

```bash
./mvnw spring-boot:run
```

Se o start falhar no Windows com `Unable to establish loopback connection`, troque os passos 1 e 2 por `docker compose --profile app up -d --build`.

**3. Capture as evidências**

```bash
bash docs/gerar-evidencias.sh
```

O script faz as duas capturas em uma passada: um arquivo por chamada da API, cada um com o método, o caminho, a resposta JSON e o código HTTP, e três consultas direto no PostgreSQL, que são os arquivos `00-*`.

> Rode o script de novo depois de **qualquer migração nova**: as evidências do banco mostram o estado dele no momento da captura, e é assim que elas deixam de bater com as migrações.

**4. Rode os testes**

```bash
./mvnw test
```

## O que cada evidência mostra

| Arquivo | O que comprova |
| :--- | :--- |
| `00-migracoes-flyway.txt` | As seis migrações aplicadas, consultadas direto no PostgreSQL |
| `00-dados-carregados.txt` | O volume real carregado da SPTrans — 12 linhas, 276 paradas, 282 itinerários — e as 12 linhas, uma a uma |
| `00-classificacao-ml.txt` | A classificação do KMeans dentro do banco, ligando as duas disciplinas |
| `00-testes.txt` | A suíte: 60 testes, 0 falhas, detalhados por classe |
| `01-saude.txt` | Aplicação no ar e conectada ao PostgreSQL |
| `02-migracoes.txt` | As seis migrações do Flyway aplicadas, com checksum e data |
| `03-painel-totem.txt` | **O endpoint central**: a tela inteira do totem em uma chamada — parada, previsões, carrossel, referências urbanas e o texto falado |
| `04-painel-por-qrcode.txt` | A ponte físico-digital: o mesmo painel a partir do QR Code lido no ponto |
| `05-totem-manutencao.txt` | A regra de negócio que impede um totem fora de operação de exibir informação (HTTP 409) |
| `06-paradas-proximas.txt` | Busca por proximidade com distância e tempo a pé |
| `07-busca-linhas.txt` | Busca unificada por número da linha |
| `08-trajeto-da-linha.txt` | Itinerário ordenado, com dados reais da SPTrans |
| `09-previsoes.txt` | Previsões vigentes, cada uma com a origem do dado |
| `10-rota-de-hoje.txt` | O card Rota de Hoje do aplicativo |
| `11-pontos-interesse.txt` | Referências urbanas próximas da parada |
| `12-totens-atencao.txt` | Monitoramento: totens que pararam de sincronizar |
| `13-erro-404.txt` | Formato padronizado de erro para recurso inexistente |
| `14-erro-409-raio.txt` | Regra de negócio violada |
| `15-erro-400-validacao.txt` | Validação de entrada, apontando cada campo inválido |
| `16-openapi.json` | A especificação OpenAPI 3 completa, gerada a partir do código |

## Capturas de tela

Cinco capturas complementam os arquivos gerados pelo script, mostrando a aplicação em funcionamento:

| Arquivo | O que mostra |
| --- | --- |
| `swagger-ui.png` | A documentação OpenAPI 3.1 servida em `/swagger-ui.html`, com as 6 áreas da API |
| `swagger-painel-totem.png` | O endpoint do painel do totem expandido, com a descrição e o exemplo de resposta |
| `migracoes-aplicadas.png` | As 6 migrações do Flyway em `/actuator/flyway`, com checksum, estado e data de aplicação |
| `testes-verdes.png` | A saída de `./mvnw test`, com os 60 testes |
| `banco-populado.png` | Uma consulta no banco com as linhas, paradas, itinerários e totens reais carregados |

As três consultas abaixo são as mesmas que o script executa; estão aqui para quem quiser rodá-las à mão.

### Contagem dos dados carregados

```bash
docker exec -it sbp-postgres psql -U sbp -d sbp -c "SELECT (SELECT COUNT(*) FROM tb_linha) AS linhas, (SELECT COUNT(*) FROM tb_parada) AS paradas, (SELECT COUNT(*) FROM tb_itinerario) AS itinerarios, (SELECT COUNT(*) FROM tb_totem) AS totens, (SELECT COUNT(*) FROM tb_ponto_interesse) AS pontos_interesse;"
```

### Histórico do Flyway

```bash
docker exec -it sbp-postgres psql -U sbp -d sbp -c "SELECT installed_rank, version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
```

### As paradas prioritárias segundo a análise de Machine Learning

```bash
docker exec -it sbp-postgres psql -U sbp -d sbp -c "SELECT prioridade_totem, COUNT(*) FROM tb_parada GROUP BY prioridade_totem ORDER BY 1;"
```
