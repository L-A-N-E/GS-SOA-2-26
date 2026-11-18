package br.com.fiap.sbp.domain.enums;

/** Situação operacional de um totem instalado na calçada. */
public enum StatusTotem {

    ATIVO("Em operação normal"),
    MANUTENCAO("Fora do ar para manutenção"),
    SEM_COMUNICACAO("Sem sincronizar há mais de 15 minutos"),
    INATIVO("Desativado ou ainda não instalado");

    private final String descricao;

    StatusTotem(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Um totem só exibe informação confiável ao cidadão quando está ativo. */
    public boolean operacional() {
        return this == ATIVO;
    }
}
