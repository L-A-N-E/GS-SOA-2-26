package br.com.fiap.sbp.exception;

/** Lancada quando o recurso pedido nao existe. Resulta em HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public RecursoNaoEncontradoException(String recurso, Object identificador) {
        super(recurso + " não encontrado(a) para o identificador: " + identificador);
    }
}
