package br.com.fiap.sbp.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Dados para cadastrar ou atualizar uma parada de ônibus")
public record ParadaRequest(

        @Schema(description = "Código da parada no GTFS da SPTrans", example = "340015890")
        @NotBlank(message = "O código GTFS da parada é obrigatório")
        @Size(max = 20, message = "O código GTFS deve ter no máximo 20 caracteres")
        String codigoGtfs,

        @Schema(description = "Nome oficial da parada", example = "Av. Paulista, 1578")
        @NotBlank(message = "O nome da parada é obrigatório")
        @Size(max = 150)
        String nome,

        @Schema(description = "Marco da rua que o cidadão reconhece",
                example = "Em frente ao MASP")
        @Size(max = 200)
        String referenciaUrbana,

        @Schema(description = "Latitude em graus decimais", example = "-23.561414")
        @NotNull(message = "A latitude é obrigatória")
        @DecimalMin(value = "-90.0", message = "Latitude inválida")
        @DecimalMax(value = "90.0", message = "Latitude inválida")
        Double latitude,

        @Schema(description = "Longitude em graus decimais", example = "-46.655881")
        @NotNull(message = "A longitude é obrigatória")
        @DecimalMin(value = "-180.0", message = "Longitude inválida")
        @DecimalMax(value = "180.0", message = "Longitude inválida")
        Double longitude,

        @Schema(description = "Parada com acesso para cadeira de rodas", example = "true")
        boolean acessivelCadeirante,

        @Schema(description = "Parada com cobertura contra sol e chuva", example = "true")
        boolean possuiCobertura
) {
}
