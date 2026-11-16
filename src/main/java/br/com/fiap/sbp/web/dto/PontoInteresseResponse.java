package br.com.fiap.sbp.web.dto;

import br.com.fiap.sbp.domain.enums.CategoriaPontoInteresse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Referência urbana próxima a parada")
public record PontoInteresseResponse(

        @Schema(example = "1") Long id,
        @Schema(example = "Hospital das Clinicas") String nome,
        @Schema(example = "HOSPITAL") CategoriaPontoInteresse categoria,

        @Schema(description = "Descrição da categoria em linguagem simples",
                example = "Hospital ou unidade de saúde")
        String descricaoCategoria,

        @Schema(example = "-23.557640") Double latitude,
        @Schema(example = "-46.669960") Double longitude,
        @Schema(example = "Av. Dr. Enéas Carvalho de Aguiar, 255") String endereco,

        @Schema(description = "Distância em quilômetros até a parada consultada, quando aplicável",
                example = "0.42")
        Double distanciaKm
) {
}
