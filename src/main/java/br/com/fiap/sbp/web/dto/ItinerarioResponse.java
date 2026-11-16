package br.com.fiap.sbp.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Posição de uma parada no trajeto de uma linha")
public record ItinerarioResponse(

        @Schema(example = "1") Long id,
        @Schema(description = "Posição da parada no trajeto, começando em 1", example = "12")
        Integer ordem,
        @Schema(description = "Minutos desde a origem da linha, segundo o quadro programado",
                example = "23")
        Integer minutosDesdeOrigem,
        @Schema(description = "Primeira parada do trajeto", example = "false")
        boolean origem,
        ParadaResponse parada
) {
}
