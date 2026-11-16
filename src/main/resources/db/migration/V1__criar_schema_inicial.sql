-- =====================================================================
-- S.B.P. - Smart Bus Point
-- V1 - Schema inicial do dominio
--
-- O Flyway e o dono do schema. O Hibernate roda com ddl-auto=validate e
-- apenas confere se o mapeamento das entidades bate com o que esta aqui.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Linhas de onibus da rede municipal (routes do GTFS)
-- ---------------------------------------------------------------------
CREATE TABLE tb_linha (
    id                     BIGSERIAL       PRIMARY KEY,
    codigo                 VARCHAR(10)     NOT NULL,
    nome                   VARCHAR(150)    NOT NULL,
    sentido                VARCHAR(30)     NOT NULL,
    area_operacao          VARCHAR(20),
    extensao_km            DOUBLE PRECISION,
    duracao_programada_min INTEGER,
    acessivel_cadeirante   BOOLEAN         NOT NULL DEFAULT TRUE,

    CONSTRAINT uk_linha_codigo UNIQUE (codigo),
    CONSTRAINT ck_linha_extensao CHECK (extensao_km IS NULL OR extensao_km > 0),
    CONSTRAINT ck_linha_duracao  CHECK (duracao_programada_min IS NULL OR duracao_programada_min > 0)
);

COMMENT ON TABLE  tb_linha IS 'Linha de onibus. Codigos iniciados em N sao linhas noturnas do Corujao.';
COMMENT ON COLUMN tb_linha.area_operacao IS 'Uma das oito areas de operacao da SPTrans.';

-- ---------------------------------------------------------------------
-- Pontos de parada (stops do GTFS)
-- ---------------------------------------------------------------------
CREATE TABLE tb_parada (
    id                   BIGSERIAL        PRIMARY KEY,
    codigo_gtfs          VARCHAR(20)      NOT NULL,
    nome                 VARCHAR(150)     NOT NULL,
    referencia_urbana    VARCHAR(200),
    latitude             DOUBLE PRECISION NOT NULL,
    longitude            DOUBLE PRECISION NOT NULL,
    acessivel_cadeirante BOOLEAN          NOT NULL DEFAULT FALSE,
    possui_cobertura     BOOLEAN          NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_parada_codigo_gtfs UNIQUE (codigo_gtfs),
    CONSTRAINT ck_parada_latitude  CHECK (latitude  BETWEEN -90  AND 90),
    CONSTRAINT ck_parada_longitude CHECK (longitude BETWEEN -180 AND 180)
);

COMMENT ON COLUMN tb_parada.referencia_urbana IS
    'Marco da rua que o cidadao reconhece antes do nome oficial da parada.';

-- ---------------------------------------------------------------------
-- Itinerario: a sequencia ordenada de paradas de cada linha
-- ---------------------------------------------------------------------
CREATE TABLE tb_itinerario (
    id                   BIGSERIAL PRIMARY KEY,
    linha_id             BIGINT    NOT NULL,
    parada_id            BIGINT    NOT NULL,
    ordem                INTEGER   NOT NULL,
    minutos_desde_origem INTEGER,

    CONSTRAINT fk_itinerario_linha  FOREIGN KEY (linha_id)  REFERENCES tb_linha (id)  ON DELETE CASCADE,
    CONSTRAINT fk_itinerario_parada FOREIGN KEY (parada_id) REFERENCES tb_parada (id) ON DELETE CASCADE,
    CONSTRAINT uk_itinerario_linha_ordem UNIQUE (linha_id, ordem),
    CONSTRAINT ck_itinerario_ordem CHECK (ordem > 0)
);

-- ---------------------------------------------------------------------
-- Totens instalados na calcada
-- ---------------------------------------------------------------------
CREATE TABLE tb_totem (
    id                   BIGSERIAL   PRIMARY KEY,
    codigo               VARCHAR(20) NOT NULL,
    parada_id            BIGINT      NOT NULL,
    status               VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    token_qr_code        VARCHAR(64),
    versao_firmware      VARCHAR(20),
    possui_audio_tts     BOOLEAN     NOT NULL DEFAULT TRUE,
    possui_braille       BOOLEAN     NOT NULL DEFAULT TRUE,
    instalado_em         TIMESTAMP,
    ultima_sincronizacao TIMESTAMP,

    CONSTRAINT fk_totem_parada FOREIGN KEY (parada_id) REFERENCES tb_parada (id),
    CONSTRAINT uk_totem_codigo UNIQUE (codigo),
    CONSTRAINT uk_totem_parada UNIQUE (parada_id),
    CONSTRAINT uk_totem_token  UNIQUE (token_qr_code)
);

COMMENT ON COLUMN tb_totem.token_qr_code IS
    'Token do QR Code impresso no totem: a ponte entre o ponto fisico e o celular.';

-- ---------------------------------------------------------------------
-- Previsoes de chegada
-- ---------------------------------------------------------------------
CREATE TABLE tb_previsao_chegada (
    id                BIGSERIAL   PRIMARY KEY,
    linha_id          BIGINT      NOT NULL,
    parada_id         BIGINT      NOT NULL,
    minutos_estimados INTEGER     NOT NULL,
    origem            VARCHAR(20) NOT NULL,
    lotacao           VARCHAR(20) NOT NULL DEFAULT 'DESCONHECIDO',
    prefixo_veiculo   VARCHAR(10),
    gerada_em         TIMESTAMP   NOT NULL,

    CONSTRAINT fk_previsao_linha  FOREIGN KEY (linha_id)  REFERENCES tb_linha (id)  ON DELETE CASCADE,
    CONSTRAINT fk_previsao_parada FOREIGN KEY (parada_id) REFERENCES tb_parada (id) ON DELETE CASCADE,
    CONSTRAINT ck_previsao_minutos CHECK (minutos_estimados >= 0)
);

COMMENT ON COLUMN tb_previsao_chegada.origem IS
    'TEMPO_REAL, GTFS_PROGRAMADO ou MODELO_ML. O cidadao tem o direito de saber a procedencia.';

-- ---------------------------------------------------------------------
-- Referencias urbanas exibidas no mapa do totem
-- ---------------------------------------------------------------------
CREATE TABLE tb_ponto_interesse (
    id        BIGSERIAL        PRIMARY KEY,
    nome      VARCHAR(150)     NOT NULL,
    categoria VARCHAR(30)      NOT NULL,
    latitude  DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    endereco  VARCHAR(200),

    CONSTRAINT ck_poi_latitude  CHECK (latitude  BETWEEN -90  AND 90),
    CONSTRAINT ck_poi_longitude CHECK (longitude BETWEEN -180 AND 180)
);

-- ---------------------------------------------------------------------
-- Cidadaos cadastrados no aplicativo (registros ficticios)
-- ---------------------------------------------------------------------
CREATE TABLE tb_cidadao (
    id                        BIGSERIAL    PRIMARY KEY,
    nome                      VARCHAR(120) NOT NULL,
    email                     VARCHAR(150) NOT NULL,
    prefere_alto_contraste    BOOLEAN      NOT NULL DEFAULT FALSE,
    prefere_leitor_tela       BOOLEAN      NOT NULL DEFAULT FALSE,
    prefere_alerta_vibratorio BOOLEAN      NOT NULL DEFAULT TRUE,
    cadastrado_em             TIMESTAMP    NOT NULL,

    CONSTRAINT uk_cidadao_email UNIQUE (email)
);

-- ---------------------------------------------------------------------
-- Rotinas programadas: o trajeto habitual do cidadao
-- ---------------------------------------------------------------------
CREATE TABLE tb_rotina_programada (
    id                    BIGSERIAL PRIMARY KEY,
    cidadao_id            BIGINT    NOT NULL,
    linha_id              BIGINT    NOT NULL,
    parada_embarque_id    BIGINT    NOT NULL,
    parada_desembarque_id BIGINT    NOT NULL,
    horario_habitual      TIME      NOT NULL,
    alerta_desembarque    BOOLEAN   NOT NULL DEFAULT TRUE,
    ativa                 BOOLEAN   NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_rotina_cidadao     FOREIGN KEY (cidadao_id)            REFERENCES tb_cidadao (id) ON DELETE CASCADE,
    CONSTRAINT fk_rotina_linha       FOREIGN KEY (linha_id)              REFERENCES tb_linha (id),
    CONSTRAINT fk_rotina_embarque    FOREIGN KEY (parada_embarque_id)    REFERENCES tb_parada (id),
    CONSTRAINT fk_rotina_desembarque FOREIGN KEY (parada_desembarque_id) REFERENCES tb_parada (id),
    CONSTRAINT ck_rotina_paradas_diferentes CHECK (parada_embarque_id <> parada_desembarque_id)
);

CREATE TABLE tb_rotina_dia (
    rotina_id  BIGINT      NOT NULL,
    dia_semana VARCHAR(10) NOT NULL,

    CONSTRAINT fk_rotina_dia_rotina FOREIGN KEY (rotina_id) REFERENCES tb_rotina_programada (id) ON DELETE CASCADE,
    CONSTRAINT pk_rotina_dia PRIMARY KEY (rotina_id, dia_semana)
);
