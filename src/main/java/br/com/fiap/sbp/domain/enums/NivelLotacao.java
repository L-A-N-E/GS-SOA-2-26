package br.com.fiap.sbp.domain.enums;

/** Lotacao do veiculo, exibida no totem e no aplicativo. */
public enum NivelLotacao {

    VAZIO("Lugares sentados disponíveis"),
    MODERADO("Poucos lugares sentados"),
    LOTADO("Somente em pé"),
    DESCONHECIDO("Sem informação de lotação");

    private final String descricao;

    NivelLotacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
