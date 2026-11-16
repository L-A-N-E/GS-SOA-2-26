package br.com.fiap.sbp.web.dto;

import br.com.fiap.sbp.domain.enums.NivelLotacao;
import br.com.fiap.sbp.domain.enums.OrigemPrevisao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;


@Schema(description = "Previsão de chegada enviada ao S.B.P.")
public record PrevisaoRequest(

        @Schema(description = "Identificador da linha", example = "1")
        @NotNull(message = "A linha é obrigatória")
        Long linhaId,

        @Schema(description = "Identificador da parada", example = "1")
        @NotNull(message = "A parada é obrigatória")
        Long paradaId,

        @Schema(description = "Minutos até a chegada. Zero significa que o ônibus está no ponto",
                example = "7")
        @NotNull(message = "O tempo estimado em minutos é obrigatório")
        @PositiveOrZero(message = "O tempo estimado não pode ser negativo")
        Integer minutosEstimados,

        @Schema(description = "De onde veio a informação", example = "TEMPO_REAL")
        @NotNull(message = "A origem da previsão é obrigatória")
        OrigemPrevisao origem,

        @Schema(description = "Lotação do veículo", example = "MODERADO")
        NivelLotacao lotacao,

        @Schema(description = "Prefixo do veículo", example = "68014")
        @Size(max = 10)
        String prefixoVeiculo,

        @Schema(description = "Limite inferior da faixa de espera, em minutos. "
                + "Usado quando a origem e MODELO_ML. Se informado, o limite superior "
                + "também é obrigatório.", example = "8")
        @PositiveOrZero(message = "O limite inferior da faixa não pode ser negativo")
        Integer faixaMinMinutos,

        @Schema(description = "Limite superior da faixa de espera, em minutos", example = "25")
        @PositiveOrZero(message = "O limite superior da faixa não pode ser negativo")
        Integer faixaMaxMinutos
) {
}
