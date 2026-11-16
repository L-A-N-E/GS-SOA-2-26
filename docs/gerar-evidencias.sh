#!/usr/bin/env bash
# =====================================================================
# S.B.P. - Smart Bus Point
# Captura as evidencias de funcionamento da API em docs/evidencias/
#
# Pre-requisitos: o banco no ar (docker compose up -d) e a aplicacao
# rodando (./mvnw spring-boot:run). Captura tanto as respostas da API
# quanto as consultas ao banco, em uma passada so.
#
# Uso:  bash docs/gerar-evidencias.sh
# =====================================================================
set -u

BASE="${SBP_BASE_URL:-http://localhost:8080}"
SAIDA="$(dirname "$0")/evidencias"
mkdir -p "$SAIDA"

requisicao() {
    local nome="$1" metodo="$2" caminho="$3"
    shift 3
    local arquivo="$SAIDA/$nome.txt"
    {
        echo "### $metodo $caminho"
        echo
        curl -s -X "$metodo" "$BASE$caminho" -H 'Accept: application/json' "$@" \
             -w '\n\n--- HTTP %{http_code} em %{time_total}s ---\n'
    } > "$arquivo" 2>&1
    printf '%-42s -> %s\n' "$metodo $caminho" "$nome.txt"
}

echo "Capturando evidencias de $BASE"
echo

# ---------------------------------------------------------------------
# Previsoes expiram em 5 minutos por regra de negocio, entao antes de
# capturar o painel registramos previsoes novas pela propria API. Em
# producao quem faz isso e a integracao com a API Olho Vivo e o servico
# que aplica o modelo de previsao.
# ---------------------------------------------------------------------
semear() {
    curl -s -o /dev/null -X POST "$BASE/api/previsoes" \
         -H 'Content-Type: application/json' -d "$1"
}
echo "Registrando previsoes recentes..."
semear '{"linhaId":5,"paradaId":115,"minutosEstimados":2,"origem":"TEMPO_REAL","lotacao":"MODERADO","prefixoVeiculo":"31207"}'
semear '{"linhaId":6,"paradaId":115,"minutosEstimados":0,"origem":"TEMPO_REAL","lotacao":"VAZIO","prefixoVeiculo":"31455"}'
semear '{"linhaId":5,"paradaId":115,"minutosEstimados":16,"origem":"GTFS_PROGRAMADO"}'
semear '{"linhaId":6,"paradaId":115,"minutosEstimados":24,"origem":"MODELO_ML","faixaMinMinutos":13,"faixaMaxMinutos":37}'
semear '{"linhaId":1,"paradaId":1,"minutosEstimados":7,"origem":"TEMPO_REAL","lotacao":"LOTADO","prefixoVeiculo":"68014"}'
semear '{"linhaId":1,"paradaId":1,"minutosEstimados":21,"origem":"GTFS_PROGRAMADO"}'
echo

# --- saude e migracoes ---
requisicao 01-saude            GET /actuator/health
requisicao 02-migracoes        GET /actuator/flyway

# --- o endpoint central: a tela do totem ---
requisicao 03-painel-totem     GET /api/totens/SBP-0003/painel
requisicao 04-painel-por-qrcode GET /api/totens/qrcode/sbp-c3d4e5f6a1b2

# --- regra de negocio: totem fora de operacao nao exibe painel (409) ---
requisicao 05-totem-manutencao GET /api/totens/SBP-0004/painel

# --- jornada do cidadao ---
requisicao 06-paradas-proximas GET "/api/paradas/proximas?latitude=-23.4319&longitude=-46.7871&raioKm=1&limite=5"
requisicao 07-busca-linhas     GET "/api/linhas?termo=1177"
requisicao 08-trajeto-da-linha GET /api/paradas/linha/1
requisicao 09-previsoes        GET /api/previsoes/parada/115
requisicao 10-rota-de-hoje     GET /api/cidadaos/1/rota-de-hoje
requisicao 11-pontos-interesse GET "/api/pontos-interesse/parada/115?raioKm=2&limite=5"

# --- monitoramento da operacao ---
requisicao 12-totens-atencao   GET /api/totens/atencao

# --- tratamento de erros ---
requisicao 13-erro-404         GET /api/paradas/999999
requisicao 14-erro-409-raio    GET "/api/paradas/proximas?latitude=-23.55&longitude=-46.63&raioKm=50"

# --- validacao de entrada (400 com os campos invalidos) ---
{
    echo "### POST /api/paradas  (corpo invalido de proposito)"
    echo
    curl -s -X POST "$BASE/api/paradas" \
         -H 'Content-Type: application/json' \
         -d '{"codigoGtfs":"","nome":"","latitude":120,"longitude":null}' \
         -w '\n\n--- HTTP %{http_code} ---\n'
} > "$SAIDA/15-erro-400-validacao.txt" 2>&1
printf '%-42s -> %s\n' "POST /api/paradas (invalido)" "15-erro-400-validacao.txt"

# --- especificacao OpenAPI completa ---
curl -s "$BASE/v3/api-docs" -o "$SAIDA/16-openapi.json"
printf '%-42s -> %s\n' "GET /v3/api-docs" "16-openapi.json"

# ---------------------------------------------------------------------
# Evidencias tiradas direto do banco. Ficam no mesmo script para que a
# captura da API e a do banco nunca fiquem desencontradas: antes elas
# eram feitas a mao, e um arquivo chegou a mostrar 4 migracoes depois
# que a V5 e a V6 ja existiam.
# ---------------------------------------------------------------------
PSQL_CONTAINER="${SBP_PG_CONTAINER:-sbp-postgres}"

consulta() {
    docker exec -i "$PSQL_CONTAINER" psql -U sbp -d sbp -c "$1" 2>&1
}

if docker exec "$PSQL_CONTAINER" true >/dev/null 2>&1; then
    echo
    echo "Capturando evidencias do banco ($PSQL_CONTAINER)..."

    {
        echo "### Migracoes aplicadas pelo Flyway no PostgreSQL 16"
        echo
        consulta "SELECT installed_rank, version, description, type, success FROM flyway_schema_history ORDER BY installed_rank;"
    } > "$SAIDA/00-migracoes-flyway.txt"
    printf '%-42s -> %s\n' "psql flyway_schema_history" "00-migracoes-flyway.txt"

    {
        echo "### Carga de dados reais da SPTrans (migracoes V4 e V5)"
        echo
        consulta "SELECT (SELECT COUNT(*) FROM tb_linha) AS linhas, (SELECT COUNT(*) FROM tb_parada) AS paradas, (SELECT COUNT(*) FROM tb_itinerario) AS itinerarios, (SELECT COUNT(*) FROM tb_totem) AS totens, (SELECT COUNT(*) FROM tb_ponto_interesse) AS pontos_interesse, (SELECT COUNT(*) FROM tb_previsao_chegada) AS previsoes, (SELECT COUNT(*) FROM tb_cidadao) AS cidadaos, (SELECT COUNT(*) FROM tb_rotina_programada) AS rotinas;"
        echo
        echo "### Amostra das linhas reais carregadas"
        consulta "SELECT codigo, nome, area_operacao, extensao_km, duracao_programada_min FROM tb_linha ORDER BY codigo;"
    } > "$SAIDA/00-dados-carregados.txt"
    printf '%-42s -> %s\n' "psql volume carregado" "00-dados-carregados.txt"

    {
        echo "### Classificacao das paradas por prioridade de instalacao do totem"
        echo "### (migracao V3 + carga V4)"
        echo
        consulta "SELECT prioridade_totem, grupo_ml, COUNT(*) FROM tb_parada GROUP BY prioridade_totem, grupo_ml ORDER BY 1;"
        echo
        echo "### As paradas de prioridade maxima que ainda nao tem totem"
        consulta "SELECT p.codigo_gtfs, p.nome, p.grupo_ml FROM tb_parada p LEFT JOIN tb_totem t ON t.parada_id = p.id WHERE p.prioridade_totem LIKE '1%' AND t.id IS NULL ORDER BY p.nome LIMIT 10;"
    } > "$SAIDA/00-classificacao-ml.txt"
    printf '%-42s -> %s\n' "psql classificacao de ML" "00-classificacao-ml.txt"
else
    echo
    echo "AVISO: container '$PSQL_CONTAINER' nao encontrado."
    echo "       As evidencias 00-migracoes-flyway, 00-dados-carregados e"
    echo "       00-classificacao-ml nao foram atualizadas. Suba o banco com"
    echo "       'docker compose up -d' e rode este script de novo."
fi

echo
echo "Evidencias gravadas em: $SAIDA"
echo "Falta anexar os prints de tela do Swagger UI (http://localhost:8080/swagger-ui.html)."
