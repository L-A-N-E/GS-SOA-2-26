package br.com.fiap.sbp.domain.entity;

import br.com.fiap.sbp.domain.enums.AreaOperacao;
import br.com.fiap.sbp.domain.enums.SentidoLinha;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Linha de onibus da rede municipal.
 *
 * <p>Corresponde a uma route do GTFS da SPTrans. O codigo segue o padrao da
 * cidade (por exemplo 8000-10); linhas iniciadas em N sao as linhas noturnas do
 * Corujao, que tem intervalo de espera muito maior e por isso recebem tratamento
 * proprio no painel do totem.</p>
 */
@Entity
@Table(name = "tb_linha", indexes = {
        @Index(name = "ix_linha_codigo", columnList = "codigo", unique = true),
        @Index(name = "ix_linha_area", columnList = "area_operacao")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Linha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O código da linha é obrigatório")
    @Size(max = 10, message = "O código da linha deve ter no máximo 10 caracteres")
    @Column(name = "codigo", nullable = false, unique = true, length = 10)
    private String codigo;

    @NotBlank(message = "O nome da linha é obrigatório")
    @Size(max = 150)
    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @NotNull(message = "O sentido da linha é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "sentido", nullable = false, length = 30)
    private SentidoLinha sentido;

    @Enumerated(EnumType.STRING)
    @Column(name = "area_operacao", length = 20)
    private AreaOperacao areaOperacao;

    @Positive(message = "A extensão deve ser maior que zero")
    @Column(name = "extensao_km")
    private Double extensaoKm;

    @Positive(message = "A duração programada deve ser maior que zero")
    @Column(name = "duracao_programada_min")
    private Integer duracaoProgramadaMin;

    @Builder.Default
    @Column(name = "acessivel_cadeirante", nullable = false)
    private boolean acessivelCadeirante = true;

    @Builder.Default
    @OneToMany(mappedBy = "linha", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Itinerario> itinerarios = new ArrayList<>();

    /**
     * Indica se e uma linha noturna (Corujao). O S.B.P. destaca essas linhas
     * porque a espera media passa de cerca de 15 para cerca de 60 minutos.
     */
    public boolean noturna() {
        return codigo != null && codigo.toUpperCase().startsWith("N");
    }

    /** Velocidade media programada, usada para estimar o tempo ate a proxima parada. */
    public Double velocidadeMediaKmh() {
        if (extensaoKm == null || duracaoProgramadaMin == null || duracaoProgramadaMin <= 0) {
            return null;
        }
        return extensaoKm / (duracaoProgramadaMin / 60.0);
    }

    /** Mantem os dois lados da associacao coerentes. */
    public void adicionarItinerario(Itinerario itinerario) {
        itinerarios.add(itinerario);
        itinerario.setLinha(this);
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) return true;
        if (!(outro instanceof Linha linha)) return false;
        return id != null && id.equals(linha.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
