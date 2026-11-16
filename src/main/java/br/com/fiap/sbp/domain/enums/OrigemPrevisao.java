package br.com.fiap.sbp.domain.enums;

/**
 * De onde veio a previsao exibida ao cidadao.
 * A ordem do enum representa a confiabilidade, da maior para a menor.
 */
public enum OrigemPrevisao {

    TEMPO_REAL("Posição real do veículo (API Olho Vivo)", 1),
    GTFS_PROGRAMADO("Quadro de horários publicado pela SPTrans", 2),
    MODELO_ML("Estimativa do modelo de Machine Learning do S.B.P.", 3);

    private final String descricao;
    private final int confiabilidade;

    OrigemPrevisao(String descricao, int confiabilidade) {
        this.descricao = descricao;
        this.confiabilidade = confiabilidade;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getConfiabilidade() {
        return confiabilidade;
    }

    /** Indica se a previsao e uma estimativa, e nao uma observacao. */
    public boolean estimada() {
        return this == MODELO_ML;
    }
}
