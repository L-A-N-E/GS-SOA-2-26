package br.com.fiap.sbp.domain.enums;

/** Situacao operacional de um totem instalado na calcada. */
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

    /** Um totem so exibe informacao confiavel ao cidadao quando esta ativo. */
    public boolean operacional() {
        return this == ATIVO;
    }
}
