package br.com.fiap.sbp.domain.enums;

/**
 * Categorias de referência urbana exibidas no mapa do totem: os marcos que o
 * passageiro reconhece na rua, e não apenas o nome da parada.
 */
public enum CategoriaPontoInteresse {

    HOSPITAL("Hospital ou unidade de saúde"),
    SHOPPING("Shopping center"),
    ESTACAO_METRO("Estação de metrô"),
    ESTACAO_TREM("Estação de trem (CPTM)"),
    TERMINAL_ONIBUS("Terminal de ônibus"),
    ESCOLA("Escola ou universidade"),
    TEATRO("Teatro ou equipamento cultural"),
    PARQUE("Parque ou área verde"),
    ORGAO_PUBLICO("Órgão público ou serviço ao cidadão");

    private final String descricao;

    CategoriaPontoInteresse(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
