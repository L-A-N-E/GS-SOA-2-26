-- =====================================================================
-- S.B.P. - Smart Bus Point
-- V5 - Ajuste do piloto dos totens
--
-- Ao exercitar a API percebemos dois problemas no recorte inicial:
--
-- 1. Os tres totens do piloto ficaram em paradas perifericas, enquanto as
--    referencias urbanas carregadas sao equipamentos do centro de Sao Paulo.
--    O painel do totem abria sem nenhuma referencia proxima, que e
--    justamente o recurso que ajuda o cidadao a se localizar pela rua.
--
-- 2. A referencia urbana da parada 301790 repetia o proprio nome da parada,
--    deixando o texto do botao sonoro redundante quando lido em voz alta.
--
-- Esta migracao coloca um totem ativo no centro, onde as referencias existem,
-- e mantem um totem em manutencao para que a regra que bloqueia o painel de
-- um equipamento fora de operacao continue demonstravel.
-- =====================================================================

-- O totem da Av. Prestes Maia fica a cerca de 200 m da Estacao da Luz e a
-- menos de 1,5 km do Theatro Municipal, da Se e do Terminal Pq. D. Pedro II.
UPDATE tb_totem
   SET status = 'ATIVO'
 WHERE codigo = 'SBP-0003';

-- Novo totem em manutencao, em uma parada de bairro, preservando o cenario
-- de equipamento fora de operacao.
INSERT INTO tb_totem (codigo, parada_id, status, token_qr_code, versao_firmware,
                      possui_audio_tts, possui_braille, instalado_em, ultima_sincronizacao)
SELECT 'SBP-0004', p.id, 'MANUTENCAO', 'sbp-d4e5f6a1b2c3', '1.3.9',
       TRUE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM tb_parada p
WHERE p.codigo_gtfs = '301730';

-- A parada do novo totem tambem foi adaptada
UPDATE tb_parada
   SET acessivel_cadeirante = TRUE,
       possui_cobertura     = TRUE,
       referencia_urbana    = 'Sentido centro, próximo ao número 262'
 WHERE codigo_gtfs = '301730';

-- Remove a redundancia do texto lido em voz alta
UPDATE tb_parada
   SET referencia_urbana = 'Plataforma de embarque'
 WHERE codigo_gtfs = '301790';
