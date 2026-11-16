-- =====================================================================
-- S.B.P. - Smart Bus Point
-- V6 - Faixa de espera nas previsoes
--
-- Quando a previsao vem do modelo de Machine Learning, anunciar um numero
-- unico da ao cidadao uma precisao que o modelo nao tem: o erro medio e de
-- cerca de 7,6 minutos. O modelo passou a publicar tambem uma faixa calibrada
-- por predicao conformal, com cobertura verificada de 80% - a espera real cai
-- dentro dela em 4 de cada 5 vezes.
--
-- As colunas ficam nulas para previsoes de telemetria e de quadro publicado,
-- que sao pontuais por natureza. Por isso sao anulaveis, e as restricoes
-- abaixo so valem quando a faixa existe.
-- =====================================================================

ALTER TABLE tb_previsao_chegada ADD COLUMN faixa_min_minutos INTEGER;
ALTER TABLE tb_previsao_chegada ADD COLUMN faixa_max_minutos INTEGER;

COMMENT ON COLUMN tb_previsao_chegada.faixa_min_minutos IS
    'Limite inferior da faixa de espera de 80%. Nulo quando a previsao e pontual.';
COMMENT ON COLUMN tb_previsao_chegada.faixa_max_minutos IS
    'Limite superior da faixa de espera de 80%. Nulo quando a previsao e pontual.';

-- Ou a faixa vem completa, ou nao vem: meia faixa nao pode ser exibida.
ALTER TABLE tb_previsao_chegada ADD CONSTRAINT ck_previsao_faixa_completa
    CHECK ((faixa_min_minutos IS NULL AND faixa_max_minutos IS NULL)
        OR (faixa_min_minutos IS NOT NULL AND faixa_max_minutos IS NOT NULL));

-- A faixa nao pode estar invertida nem ser negativa.
ALTER TABLE tb_previsao_chegada ADD CONSTRAINT ck_previsao_faixa_ordenada
    CHECK (faixa_min_minutos IS NULL
        OR (faixa_min_minutos >= 0 AND faixa_min_minutos <= faixa_max_minutos));

-- A estimativa central precisa cair dentro da propria faixa.
ALTER TABLE tb_previsao_chegada ADD CONSTRAINT ck_previsao_faixa_contem_estimativa
    CHECK (faixa_min_minutos IS NULL
        OR (minutos_estimados BETWEEN faixa_min_minutos AND faixa_max_minutos));

-- ---------------------------------------------------------------------
-- As previsoes do modelo carregadas na V4 passam a exibir a faixa.
-- Os valores vem do artefato espera_por_linha_hora.csv, gerado pelo
-- notebook da disciplina de Machine Learning.
-- ---------------------------------------------------------------------
UPDATE tb_previsao_chegada
   SET faixa_min_minutos = 4,
       faixa_max_minutos = 19
 WHERE origem = 'MODELO_ML' AND minutos_estimados = 7;

UPDATE tb_previsao_chegada
   SET faixa_min_minutos = 11,
       faixa_max_minutos = 34
 WHERE origem = 'MODELO_ML' AND minutos_estimados = 21;
