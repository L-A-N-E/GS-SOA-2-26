package br.com.fiap.sbp.exception;

/**
 * Lançada quando a operação é sintaticamente válida mas viola uma regra do
 * domínio do S.B.P. Resulta em HTTP 409 (conflito).
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
