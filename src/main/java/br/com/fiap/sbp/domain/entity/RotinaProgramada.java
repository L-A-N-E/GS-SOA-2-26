package br.com.fiap.sbp.domain.entity;

import br.com.fiap.sbp.domain.enums.DiaSemana;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Trajeto habitual que o cidadao salvou no aplicativo.
 *
 * <p>E o que sustenta o card Rota de Hoje da tela inicial: no horario e no dia
 * certos, o aplicativo ja abre mostrando a linha que a pessoa costuma pegar, sem
 * que ela precise pesquisar nada.</p>
 */
@Entity
@Table(name = "tb_rotina_programada",
        indexes = @Index(name = "ix_rotina_cidadao", columnList = "cidadao_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RotinaProgramada {

    /** Janela, em minutos, em que a rotina e considerada proxima do horario habitual. */
    public static final int JANELA_MINUTOS = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cidadao_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_rotina_cidadao"))
    private Cidadao cidadao;

    @NotNull(message = "A linha é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "linha_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_rotina_linha"))
    private Linha linha;

    @NotNull(message = "A parada de embarque é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parada_embarque_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_rotina_embarque"))
    private Parada paradaEmbarque;

    @NotNull(message = "A parada de desembarque é obrigatória")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parada_desembarque_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_rotina_desembarque"))
    private Parada paradaDesembarque;

    @NotNull(message = "O horário habitual é obrigatório")
    @Column(name = "horario_habitual", nullable = false)
    private LocalTime horarioHabitual;

    @NotEmpty(message = "Informe ao menos um dia da semana")
    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tb_rotina_dia",
            joinColumns = @JoinColumn(name = "rotina_id",
                    foreignKey = @ForeignKey(name = "fk_rotina_dia_rotina")))
    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false, length = 10)
    private Set<DiaSemana> dias = new HashSet<>();

    @Builder.Default
    @Column(name = "alerta_desembarque", nullable = false)
    private boolean alertaDesembarque = true;

    @Builder.Default
    @Column(name = "ativa", nullable = false)
    private boolean ativa = true;

    /** Indica se a rotina deve ser lembrada no dia informado. */
    public boolean ativaEm(DayOfWeek dia) {
        return ativa && dias.contains(DiaSemana.de(dia));
    }

    /**
     * Indica se o momento informado esta dentro da janela do deslocamento
     * habitual, condicao para o card Rota de Hoje aparecer na tela inicial.
     */
    public boolean proximaDoHorario(LocalTime agora) {
        if (horarioHabitual == null || agora == null) {
            return false;
        }
        long diferenca = Math.abs(java.time.Duration.between(horarioHabitual, agora).toMinutes());
        return diferenca <= JANELA_MINUTOS;
    }
}
