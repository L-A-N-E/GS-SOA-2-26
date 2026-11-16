package br.com.fiap.sbp.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Parada encontrada na busca por proximidade, com a distância até o cidadão")
public record ParadaProximaResponse(

        @Schema(description = "Dados da parada")
        ParadaResponse parada,

        @Schema(description = "Distância em quilômetros até a coordenada informada", example = "0.184")
        double distanciaKm,

        @Schema(description = "Distância aproximada a pé, em minutos, a 5 km/h", example = "2")
        int minutosCaminhando
) {
}
