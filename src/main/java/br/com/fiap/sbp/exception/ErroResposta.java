package br.com.fiap.sbp.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Corpo padronizado de erro da API.
 *
 * <p>Toda falha, de qualquer endpoint, responde neste mesmo formato, para que o
 * aplicativo, o site e o totem tratem erros de uma unica maneira.</p>
 */
@Schema(description = "Resposta padronizada de erro da API do S.B.P.")
public record ErroResposta(

        @Schema(description = "Momento em que o erro ocorreu", example = "2026-11-19T21:35:12")
        LocalDateTime momento,

        @Schema(description = "Código HTTP", example = "404")
        int status,

        @Schema(description = "Título curto do erro", example = "Recurso não encontrado")
        String erro,

        @Schema(description = "Explicação em linguagem simples",
                example = "Parada não encontrado(a) para o identificador: 999")
        String mensagem,

        @Schema(description = "Caminho chamado", example = "/api/paradas/999")
        String caminho,

        @Schema(description = "Campos inválidos, presente apenas em erros de validação")
        Map<String, String> camposInvalidos,

        @Schema(description = "Detalhes adicionais, quando houver")
        List<String> detalhes
) {

    public static ErroResposta de(int status, String erro, String mensagem, String caminho) {
        return new ErroResposta(LocalDateTime.now(), status, erro, mensagem, caminho, null, null);
    }

    public static ErroResposta deValidacao(String caminho, Map<String, String> camposInvalidos) {
        return new ErroResposta(LocalDateTime.now(), 400, "Requisição inválida",
                "Um ou mais campos estão preenchidos de forma incorreta.",
                caminho, camposInvalidos, null);
    }
}
