package br.com.fiap.sbp.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Cidadão cadastrado no aplicativo")
public record CidadaoResponse(

        @Schema(example = "1") Long id,
        @Schema(example = "Maria de Souza") String nome,
        @Schema(example = "maria.souza@exemplo.com") String email,
        @Schema(example = "false") boolean prefereAltoContraste,
        @Schema(example = "false") boolean prefereLeitorTela,
        @Schema(example = "true") boolean prefereAlertaVibratorio,

        @Schema(description = "Cidadão que usa algum recurso assistivo", example = "false")
        boolean usaRecursoAssistivo,

        @Schema(description = "Quantidade de rotinas salvas", example = "2")
        int totalRotinas,

        @Schema(example = "2026-02-14T08:12:00") LocalDateTime cadastradoEm
) {
}
