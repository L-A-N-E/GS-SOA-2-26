package br.com.fiap.sbp.domain.entity;

import br.com.fiap.sbp.domain.enums.CategoriaPontoInteresse;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Referencia urbana exibida no mapa do totem.
 *
 * <p>Hospitais, shoppings, estacoes de metro e teatros sao os marcos que o
 * passageiro usa de fato para se localizar. Mostrar o itinerario apenas com o
 * nome das paradas nao ajuda quem nao conhece o bairro.</p>
 */
@Entity
@Table(name = "tb_ponto_interesse", indexes = {
        @Index(name = "ix_poi_categoria", columnList = "categoria"),
        @Index(name = "ix_poi_coord", columnList = "latitude,longitude")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PontoDeInteresse {

    private static final double RAIO_TERRA_KM = 6371.0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do ponto de interesse é obrigatório")
    @Size(max = 150)
    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @NotNull(message = "A categoria é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 30)
    private CategoriaPontoInteresse categoria;

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

    @Size(max = 200)
    @Column(name = "endereco", length = 200)
    private String endereco;

    /** Distancia em quilometros ate uma parada, pela formula de Haversine. */
    public double distanciaKm(double outraLatitude, double outraLongitude) {
        double lat1 = Math.toRadians(this.latitude);
        double lat2 = Math.toRadians(outraLatitude);
        double deltaLat = lat2 - lat1;
        double deltaLon = Math.toRadians(outraLongitude - this.longitude);

        double a = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(deltaLon / 2), 2);
        return 2 * RAIO_TERRA_KM * Math.asin(Math.sqrt(a));
    }
}
