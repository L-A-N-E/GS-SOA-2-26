package br.com.fiap.sbp.web.dto;

import br.com.fiap.sbp.domain.enums.StatusTotem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para instalar ou atualizar um totem do S.B.P.")
public record TotemRequest(

        @Schema(description = "Código de patrimônio do totem", example = "SBP-0001")
        @NotBlank(message = "O código do totem é obrigatório")
        @Size(max = 20)
        String codigo,

        @Schema(description = "Identificador da parada onde o totem está instalado", example = "1")
        @NotNull(message = "O totem precisa estar vinculado a uma parada")
        Long paradaId,

        @Schema(description = "Situação operacional", example = "ATIVO")
        StatusTotem status,

        @Schema(description = "Versão do firmware embarcado", example = "1.4.2")
        @Size(max = 20)
        String versaoFirmware,

        @Schema(description = "Possui botão sonoro com sintetizador de voz", example = "true")
        boolean possuiAudioTts,

        @Schema(description = "Possui identificação tátil em Braille", example = "true")
        boolean possuiBraille
) {
}
