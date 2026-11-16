package br.com.fiap.sbp.domain.enums;

/** Sentido de operacao de uma linha, conforme o campo {@code direction_id} do GTFS. */
public enum SentidoLinha {

    TERMINAL_PRINCIPAL("Terminal principal -> bairro"),
    BAIRRO("Bairro -> terminal principal"),
    CIRCULAR("Trajeto circular, sem sentido inverso");

    private final String descricao;

    SentidoLinha(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
