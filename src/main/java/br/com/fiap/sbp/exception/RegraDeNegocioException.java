package br.com.fiap.sbp.exception;

/**
 * Lancada quando a operacao e sintaticamente valida mas viola uma regra do
 * dominio do S.B.P. Resulta em HTTP 409 (conflito).
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
