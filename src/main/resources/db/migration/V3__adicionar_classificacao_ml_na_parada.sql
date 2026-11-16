-- =====================================================================
-- S.B.P. - Smart Bus Point
-- V3 - Classificacao das paradas vinda da analise de Machine Learning
--
-- A disciplina de Inteligencia Artificial & Machine Learning agrupou as
-- 21.933 paradas de Sao Paulo por oferta de linhas e tempo de espera
-- (PCA + KMeans sobre o GTFS da SPTrans) e produziu uma fila de instalacao
-- dos totens. Esta migracao traz esse resultado para dentro do dominio.
--
-- Por que duas colunas novas em vez de uma tabela separada: a classificacao
-- e um atributo da propria parada, consultado junto com ela em toda chamada
-- do painel. Uma tabela a parte so acrescentaria um JOIN na consulta mais
-- frequente da API.
-- =====================================================================

ALTER TABLE tb_parada ADD COLUMN grupo_ml         VARCHAR(60);
ALTER TABLE tb_parada ADD COLUMN prioridade_totem VARCHAR(40);

COMMENT ON COLUMN tb_parada.grupo_ml IS
    'Grupo do agrupamento KMeans: parada isolada de longa espera, hub de integracao, corredor de bairro ou parada integrada ao metro.';

COMMENT ON COLUMN tb_parada.prioridade_totem IS
    'Posicao na fila de instalacao dos totens. A prioridade 1 e de equidade: as paradas com menos de 2 linhas e quase 37 minutos de espera no pico.';

-- Consulta da operacao: quais paradas ainda sem totem estao no topo da fila
CREATE INDEX ix_parada_prioridade ON tb_parada (prioridade_totem);
