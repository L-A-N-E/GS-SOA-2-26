package br.com.fiap.sbp.web.dto;

import br.com.fiap.sbp.domain.enums.CategoriaPontoInteresse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Referência urbana exibida no mapa do totem")
public record PontoInteresseRequest(

        @Schema(example = "Hospital das Clinicas")
        @NotBlank(message = "O nome do ponto de interesse é obrigatório")
        @Size(max = 150)
        String nome,

        @Schema(example = "HOSPITAL")
        @NotNull(message = "A categoria é obrigatória")
        CategoriaPontoInteresse categoria,

        @Schema(example = "-23.557640")
        @NotNull(message = "A latitude é obrigatória")
        @DecimalMin(value = "-90.0", message = "Latitude inválida")
        @DecimalMax(value = "90.0", message = "Latitude inválida")
        Double latitude,

        @Schema(example = "-46.669960")
        @NotNull(message = "A longitude é obrigatória")
        @DecimalMin(value = "-180.0", message = "Longitude inválida")
        @DecimalMax(value = "180.0", message = "Longitude inválida")
        Double longitude,

        @Schema(example = "Av. Dr. Enéas Carvalho de Aguiar, 255")
        @Size(max = 200)
        String endereco
) {
}
