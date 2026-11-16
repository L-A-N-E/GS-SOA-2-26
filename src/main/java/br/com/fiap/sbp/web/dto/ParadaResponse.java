package br.com.fiap.sbp.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Parada de ônibus")
public record ParadaResponse(

        @Schema(example = "1") Long id,
        @Schema(example = "340015890") String codigoGtfs,
        @Schema(example = "Av. Paulista, 1578") String nome,
        @Schema(example = "Em frente ao MASP") String referenciaUrbana,
        @Schema(example = "-23.561414") Double latitude,
        @Schema(example = "-46.655881") Double longitude,
        @Schema(example = "true") boolean acessivelCadeirante,
        @Schema(example = "true") boolean possuiCobertura,

        @Schema(description = "Indica se a parada já tem um totem do S.B.P. em operação",
                example = "true")
        boolean possuiTotemAtivo,

        @Schema(description = "Grupo atribuído pelo agrupamento KMeans da análise de dados",
                example = "Hub de integração")
        String grupoMl,

        @Schema(description = "Posição na fila de instalação dos totens",
                example = "2 - alcance")
        String prioridadeTotem,

        @Schema(description = "Texto lido em voz alta pelo totem ao identificar a parada",
                example = "Parada Av. Paulista, 1578, Em frente ao MASP")
        String descricaoAcessivel
) {
}
