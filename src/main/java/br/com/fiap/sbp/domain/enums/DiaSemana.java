package br.com.fiap.sbp.domain.enums;

import java.time.DayOfWeek;

/** Dias em que uma rotina programada do cidadao deve ser lembrada. */
public enum DiaSemana {

    SEGUNDA(DayOfWeek.MONDAY),
    TERCA(DayOfWeek.TUESDAY),
    QUARTA(DayOfWeek.WEDNESDAY),
    QUINTA(DayOfWeek.THURSDAY),
    SEXTA(DayOfWeek.FRIDAY),
    SABADO(DayOfWeek.SATURDAY),
    DOMINGO(DayOfWeek.SUNDAY);

    private final DayOfWeek equivalente;

    DiaSemana(DayOfWeek equivalente) {
        this.equivalente = equivalente;
    }

    public DayOfWeek getEquivalente() {
        return equivalente;
    }

    public static DiaSemana de(DayOfWeek dia) {
        for (DiaSemana valor : values()) {
            if (valor.equivalente == dia) {
                return valor;
            }
        }
        throw new IllegalArgumentException("Dia da semana inválido: " + dia);
    }
}
