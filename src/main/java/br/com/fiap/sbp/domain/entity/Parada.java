package br.com.fiap.sbp.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Ponto de parada de onibus.
 *
 * <p>Corresponde a um stop do GTFS. Guarda tambem a referencia urbana (por
 * exemplo, em frente ao Hospital das Clinicas) porque o cidadao reconhece o
 * marco da rua antes de reconhecer o nome oficial da parada.</p>
 */
@Entity
@Table(name = "tb_parada", indexes = {
        @Index(name = "ix_parada_codigo", columnList = "codigo_gtfs", unique = true),
        @Index(name = "ix_parada_nome", columnList = "nome"),
        @Index(name = "ix_parada_coord", columnList = "latitude,longitude")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parada {

    private static final double RAIO_TERRA_KM = 6371.0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O código GTFS da parada é obrigatório")
    @Size(max = 20)
    @Column(name = "codigo_gtfs", nullable = false, unique = true, length = 20)
    private String codigoGtfs;

    @NotBlank(message = "O nome da parada é obrigatório")
    @Size(max = 150)
    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Size(max = 200)
    @Column(name = "referencia_urbana", length = 200)
    private String referenciaUrbana;

    @NotNull(message = "A latitude é obrigatória")
    @DecimalMin(value = "-90.0", message = "Latitude inválida")
    @DecimalMax(value = "90.0", message = "Latitude inválida")
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @NotNull(message = "A longitude é obrigatória")
    @DecimalMin(value = "-180.0", message = "Longitude inválida")
    @DecimalMax(value = "180.0", message = "Longitude inválida")
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Builder.Default
    @Column(name = "acessivel_cadeirante", nullable = false)
    private boolean acessivelCadeirante = false;

    @Builder.Default
    @Column(name = "possui_cobertura", nullable = false)
    private boolean possuiCobertura = false;

    /**
     * Grupo atribuido a parada pelo agrupamento KMeans da disciplina de Machine
     * Learning, a partir da oferta de linhas e da espera observada no GTFS.
     */
    @Size(max = 60)
    @Column(name = "grupo_ml", length = 60)
    private String grupoMl;

    /**
     * Posicao da parada na fila de instalacao dos totens, derivada do mesmo
     * agrupamento. E o que liga a analise de dados a decisao de onde instalar.
     */
    @Size(max = 40)
    @Column(name = "prioridade_totem", length = 40)
    private String prioridadeTotem;

    @Builder.Default
    @OneToMany(mappedBy = "parada", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Itinerario> itinerarios = new ArrayList<>();

    @OneToOne(mappedBy = "parada")
    private Totem totem;

    /**
     * Distancia em quilometros ate uma coordenada, pela formula de Haversine.
     * Usada na busca do ponto mais proximo do cidadao, no aplicativo e no site.
     */
    public double distanciaKm(double outraLatitude, double outraLongitude) {
        double lat1 = Math.toRadians(this.latitude);
        double lat2 = Math.toRadians(outraLatitude);
        double deltaLat = lat2 - lat1;
        double deltaLon = Math.toRadians(outraLongitude - this.longitude);

        double a = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(deltaLon / 2), 2);
        return 2 * RAIO_TERRA_KM * Math.asin(Math.sqrt(a));
    }

    /** Indica se a parada ja conta com um totem do S.B.P. em operacao. */
    public boolean possuiTotemAtivo() {
        return totem != null && totem.getStatus().operacional();
    }

    /** Texto curto que o totem le em voz alta ao identificar a parada. */
    public String descricaoAcessivel() {
        if (referenciaUrbana == null || referenciaUrbana.isBlank()) {
            return "Parada " + nome;
        }
        return "Parada " + nome + ", " + referenciaUrbana;
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) return true;
        if (!(outro instanceof Parada parada)) return false;
        return id != null && id.equals(parada.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
