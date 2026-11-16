package br.com.fiap.sbp.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Cadastro do cidadão no aplicativo do S.B.P.")
public record CidadaoRequest(

        @Schema(example = "Maria de Souza")
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 120)
        String nome,

        @Schema(example = "maria.souza@exemplo.com")
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 150)
        String email,

        @Schema(description = "Interface em alto contraste", example = "false")
        boolean prefereAltoContraste,

        @Schema(description = "Compatibilidade com leitor de tela", example = "false")
        boolean prefereLeitorTela,

        @Schema(description = "Alerta de desembarque por vibração", example = "true")
        boolean prefereAlertaVibratorio
) {
}
