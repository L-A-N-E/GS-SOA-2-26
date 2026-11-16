-- =====================================================================
-- S.B.P. - Smart Bus Point
-- V2 - Indices de apoio as consultas do totem e do aplicativo
--
-- Cada indice aqui existe por causa de uma consulta concreta da aplicacao,
-- e nao por precaucao. Indice sobrando custa escrita e espaco.
-- =====================================================================

-- Busca de linha pelo codigo que o cidadao conhece (8000-10)
CREATE INDEX ix_linha_codigo ON tb_linha (codigo);
CREATE INDEX ix_linha_area   ON tb_linha (area_operacao);

-- Busca de parada pelo nome, na aba Explorar
CREATE INDEX ix_parada_codigo ON tb_parada (codigo_gtfs);
CREATE INDEX ix_parada_nome   ON tb_parada (nome);

-- Busca por proximidade: a consulta filtra primeiro por uma caixa de
-- coordenadas para nao percorrer as 22 mil paradas da cidade a cada chamada.
CREATE INDEX ix_parada_coord ON tb_parada (latitude, longitude);

-- Montagem do painel do totem: quais linhas atendem esta parada
CREATE INDEX ix_itinerario_parada ON tb_itinerario (parada_id);

-- Monitoramento dos totens
CREATE INDEX ix_totem_codigo ON tb_totem (codigo);
CREATE INDEX ix_totem_status ON tb_totem (status);

-- Previsoes vigentes de uma parada, ordenadas pelo tempo de chegada.
-- E a consulta mais chamada da API: cada totem a repete a cada poucos segundos.
CREATE INDEX ix_previsao_parada ON tb_previsao_chegada (parada_id);
CREATE INDEX ix_previsao_linha  ON tb_previsao_chegada (linha_id);
CREATE INDEX ix_previsao_gerada ON tb_previsao_chegada (gerada_em);

-- Referencias urbanas proximas da parada
CREATE INDEX ix_poi_categoria ON tb_ponto_interesse (categoria);
CREATE INDEX ix_poi_coord     ON tb_ponto_interesse (latitude, longitude);

-- Card Rota de Hoje
CREATE INDEX ix_rotina_cidadao ON tb_rotina_programada (cidadao_id);
CREATE INDEX ix_cidadao_email  ON tb_cidadao (email);
