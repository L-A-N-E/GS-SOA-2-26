package br.com.fiap.sbp.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centraliza o tratamento de erros de toda a API.
 *
 * <p>Uma exceção de domínio vira sempre a mesma resposta HTTP, com uma mensagem
 * compreensível ao cidadão e sem vazar detalhes internos do servidor.</p>
 */
@RestControllerAdvice
public class ManipuladorGlobalDeErros {

    private static final Logger log = LoggerFactory.getLogger(ManipuladorGlobalDeErros.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarNaoEncontrado(RecursoNaoEncontradoException excecao,
                                                            HttpServletRequest requisicao) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErroResposta.de(404, "Recurso não encontrado",
                        excecao.getMessage(), requisicao.getRequestURI()));
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResposta> tratarRegraDeNegocio(RegraDeNegocioException excecao,
                                                             HttpServletRequest requisicao) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErroResposta.de(409, "Conflito com uma regra de negócio",
                        excecao.getMessage(), requisicao.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacao(MethodArgumentNotValidException excecao,
                                                        HttpServletRequest requisicao) {
        Map<String, String> campos = new LinkedHashMap<>();
        excecao.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(ErroResposta.deValidacao(requisicao.getRequestURI(), campos));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResposta> tratarTipoInvalido(MethodArgumentTypeMismatchException excecao,
                                                           HttpServletRequest requisicao) {
        String mensagem = "O parâmetro " + excecao.getName() + " recebeu um valor inválido: "
                + excecao.getValue();
        return ResponseEntity.badRequest().body(
                ErroResposta.de(400, "Parâmetro inválido", mensagem, requisicao.getRequestURI()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> tratarCorpoIlegivel(HttpMessageNotReadableException excecao,
                                                            HttpServletRequest requisicao) {
        return ResponseEntity.badRequest().body(
                ErroResposta.de(400, "Corpo da requisição inválido",
                        "Não foi possível interpretar o JSON enviado.", requisicao.getRequestURI()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> tratarIntegridade(DataIntegrityViolationException excecao,
                                                          HttpServletRequest requisicao) {
        log.warn("Violacao de integridade em {}: {}", requisicao.getRequestURI(), excecao.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErroResposta.de(409, "Conflito de dados",
                        "A operação viola uma restrição do banco de dados, "
                                + "como um código que já existe ou um vinculo obrigatório.",
                        requisicao.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> tratarInesperado(Exception excecao,
                                                         HttpServletRequest requisicao) {
        log.error("Erro inesperado em {}", requisicao.getRequestURI(), excecao);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErroResposta.de(500, "Erro interno",
                        "Ocorreu um erro inesperado. Tente novamente em alguns instantes.",
                        requisicao.getRequestURI()));
    }
}
