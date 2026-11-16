package br.com.fiap.sbp.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * Posicao de uma parada dentro do trajeto de uma linha.
 *
 * <p>E a associacao que permite ao totem desenhar o mapa de rota: a sequencia
 * ordenada das paradas pelas quais a linha passa.</p>
 */
@Entity
@Table(name = "tb_itinerario",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_itinerario_linha_ordem",
                columnNames = {"linha_id", "ordem"}),
        indexes = @Index(name = "ix_itinerario_parada", columnList = "parada_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Itinerario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "linha_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_itinerario_linha"))
    private Linha linha;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parada_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_itinerario_parada"))
    private Parada parada;

    @NotNull(message = "A ordem da parada no trajeto é obrigatória")
    @Positive(message = "A ordem deve começar em 1")
    @Column(name = "ordem", nullable = false)
    private Integer ordem;

    @Column(name = "minutos_desde_origem")
    private Integer minutosDesdeOrigem;

    /** Primeira parada do trajeto, de onde a linha sai. */
    public boolean origem() {
        return ordem != null && ordem == 1;
    }
}
