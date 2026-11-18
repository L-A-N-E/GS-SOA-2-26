package br.com.fiap.sbp.exception;

/** Lançada quando o recurso pedido não existe. Resulta em HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public RecursoNaoEncontradoException(String recurso, Object identificador) {
        super(recurso + " não encontrado(a) para o identificador: " + identificador);
    }
}
