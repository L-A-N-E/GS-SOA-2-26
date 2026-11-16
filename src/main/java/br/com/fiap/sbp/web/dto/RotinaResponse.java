package br.com.fiap.sbp.web.dto;

import br.com.fiap.sbp.domain.enums.DiaSemana;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;
import java.util.Set;

@Schema(description = "Trajeto habitual salvo pelo cidadão")
public record RotinaResponse(

        @Schema(example = "1") Long id,
        @Schema(example = "1") Long cidadaoId,
        @Schema(example = "8000-10") String codigoLinha,
        @Schema(example = "Term. Lapa - Term. Pq. D. Pedro II") String nomeLinha,
        @Schema(example = "Av. Paulista, 1578") String paradaEmbarque,
        @Schema(example = "Term. Pq. D. Pedro II") String paradaDesembarque,
        @Schema(example = "07:30:00") LocalTime horarioHabitual,
        Set<DiaSemana> dias,
        @Schema(example = "true") boolean alertaDesembarque,
        @Schema(example = "true") boolean ativa,

        @Schema(description = "Rotina que deve aparecer agora no card Rota de Hoje",
                example = "true")
        boolean ativaAgora
) {
}
