package br.com.fiap.sbp.web.dto;

import br.com.fiap.sbp.domain.enums.StatusTotem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Totem instalado na calçada")
public record TotemResponse(

        @Schema(example = "1") Long id,
        @Schema(example = "SBP-0001") String codigo,
        @Schema(example = "ATIVO") StatusTotem status,

        @Schema(description = "Token que o QR Code do totem carrega",
                example = "sbp-a1b2c3d4e5f6")
        String tokenQrCode,

        @Schema(example = "1.4.2") String versaoFirmware,
        @Schema(example = "true") boolean possuiAudioTts,
        @Schema(example = "true") boolean possuiBraille,

        @Schema(description = "Totem atende integralmente a Lei Brasileira de Inclusão",
                example = "true")
        boolean plenamenteAcessivel,

        @Schema(example = "2026-03-10T09:00:00") LocalDateTime instaladoEm,
        @Schema(example = "2026-11-19T21:34:50") LocalDateTime ultimaSincronizacao,

        @Schema(description = "Minutos desde a última comunicação com o backend", example = "2")
        Long minutosDesdeUltimaSincronizacao,

        @Schema(description = "Totem que a operação precisa verificar", example = "false")
        boolean precisaAtencao,

        @Schema(description = "Parada onde o totem está instalado")
        ParadaResponse parada
) {
}
