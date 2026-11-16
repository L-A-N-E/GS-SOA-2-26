package br.com.fiap.sbp.web.dto;

import br.com.fiap.sbp.domain.enums.AreaOperacao;
import br.com.fiap.sbp.domain.enums.SentidoLinha;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Dados para cadastrar ou atualizar uma linha de ônibus")
public record LinhaRequest(

        @Schema(description = "Código da linha no padrão da cidade", example = "8000-10")
        @NotBlank(message = "O código da linha é obrigatório")
        @Size(max = 10, message = "O código da linha deve ter no máximo 10 caracteres")
        String codigo,

        @Schema(description = "Nome do trajeto", example = "Term. Lapa - Term. Pq. D. Pedro II")
        @NotBlank(message = "O nome da linha é obrigatório")
        @Size(max = 150)
        String nome,

        @Schema(description = "Sentido de operação", example = "TERMINAL_PRINCIPAL")
        @NotNull(message = "O sentido da linha é obrigatório")
        SentidoLinha sentido,

        @Schema(description = "Área de operação da SPTrans", example = "OESTE")
        AreaOperacao areaOperacao,

        @Schema(description = "Extensão do trajeto em quilômetros", example = "18.4")
        @Positive(message = "A extensão deve ser maior que zero")
        Double extensaoKm,

        @Schema(description = "Duração programada do trajeto em minutos", example = "72")
        @Positive(message = "A duração programada deve ser maior que zero")
        Integer duracaoProgramadaMin,

        @Schema(description = "Frota acessível para cadeirantes", example = "true")
        boolean acessivelCadeirante
) {
}
