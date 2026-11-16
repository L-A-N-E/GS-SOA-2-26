package br.com.fiap.sbp.web.dto;

import br.com.fiap.sbp.domain.enums.NivelLotacao;
import br.com.fiap.sbp.domain.enums.OrigemPrevisao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Previsão de chegada exibida ao cidadão")
public record PrevisaoResponse(

        @Schema(example = "1") Long id,
        @Schema(example = "8000-10") String codigoLinha,
        @Schema(example = "Term. Lapa - Term. Pq. D. Pedro II") String nomeLinha,
        @Schema(example = "Av. Paulista, 1578") String nomeParada,
        @Schema(example = "7") Integer minutosEstimados,
        @Schema(example = "TEMPO_REAL") OrigemPrevisao origem,

        @Schema(description = "Explicação da origem, exibida ao cidadão",
                example = "Posição real do veículo (API Olho Vivo)")
        String descricaoOrigem,

        @Schema(example = "MODERADO") NivelLotacao lotacao,
        @Schema(example = "68014") String prefixoVeiculo,

        @Schema(description = "Limite inferior da faixa de espera, quando houver", example = "8")
        Integer faixaMinMinutos,

        @Schema(description = "Limite superior da faixa de espera, quando houver", example = "25")
        Integer faixaMaxMinutos,

        @Schema(description = "Faixa pronta para a tela do totem, ou nulo quando a previsão "
                + "e pontual", example = "8 a 25 min")
        String faixaFormatada,

        @Schema(description = "O ônibus já está na parada", example = "false")
        boolean onibusNoPonto,

        @Schema(description = "Faltam menos de 3 minutos: ocupa o card principal do carrossel",
                example = "false")
        boolean emergencia,

        @Schema(description = "Frase que o totem le em voz alta no botão tátil",
                example = "O ônibus da linha 8000-10 chega em 7 minutos.")
        String textoAcessivel,

        @Schema(example = "2026-11-19T21:34:50") LocalDateTime geradaEm
) {
}
