package br.com.fiap.sbp.web.dto;

import br.com.fiap.sbp.domain.enums.AreaOperacao;
import br.com.fiap.sbp.domain.enums.SentidoLinha;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Linha de ônibus")
public record LinhaResponse(

        @Schema(example = "1") Long id,
        @Schema(example = "8000-10") String codigo,
        @Schema(example = "Term. Lapa - Term. Pq. D. Pedro II") String nome,
        @Schema(example = "TERMINAL_PRINCIPAL") SentidoLinha sentido,
        @Schema(example = "OESTE") AreaOperacao areaOperacao,
        @Schema(example = "18.4") Double extensaoKm,
        @Schema(example = "72") Integer duracaoProgramadaMin,
        @Schema(example = "true") boolean acessivelCadeirante,

        @Schema(description = "Linha noturna do Corujão, com intervalo de espera muito maior",
                example = "false")
        boolean noturna,

        @Schema(description = "Velocidade média programada em km/h", example = "15.3")
        Double velocidadeMediaKmh
) {
}
