package br.com.fiap.sbp.web.dto;

import br.com.fiap.sbp.domain.enums.DiaSemana;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.Set;

@Schema(description = "Trajeto habitual que o cidadão salva no aplicativo")
public record RotinaRequest(

        @Schema(description = "Identificador da linha habitual", example = "1")
        @NotNull(message = "A linha é obrigatória")
        Long linhaId,

        @Schema(description = "Parada onde o cidadão embarca", example = "1")
        @NotNull(message = "A parada de embarque é obrigatória")
        Long paradaEmbarqueId,

        @Schema(description = "Parada onde o cidadão desembarca", example = "2")
        @NotNull(message = "A parada de desembarque é obrigatória")
        Long paradaDesembarqueId,

        @Schema(description = "Horário habitual do deslocamento", example = "07:30:00")
        @NotNull(message = "O horário habitual é obrigatório")
        LocalTime horarioHabitual,

        @Schema(description = "Dias da semana em que a rotina se repete",
                example = "[\"SEGUNDA\",\"TERCA\",\"QUARTA\",\"QUINTA\",\"SEXTA\"]")
        @NotEmpty(message = "Informe ao menos um dia da semana")
        Set<DiaSemana> dias,

        @Schema(description = "Avisar por vibração ao se aproximar do desembarque", example = "true")
        boolean alertaDesembarque
) {
}
