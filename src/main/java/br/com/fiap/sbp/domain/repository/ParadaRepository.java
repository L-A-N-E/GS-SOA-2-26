package br.com.fiap.sbp.domain.repository;

import br.com.fiap.sbp.domain.entity.Parada;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParadaRepository extends JpaRepository<Parada, Long> {

    Optional<Parada> findByCodigoGtfs(String codigoGtfs);

    boolean existsByCodigoGtfs(String codigoGtfs);

    Page<Parada> findByNomeContainingIgnoreCase(String nome, Pageable paginacao);

    /**
     * Pre-filtra as paradas dentro de uma caixa de coordenadas.
     *
     * <p>A distancia exata e calculada depois, em memoria, pelo metodo
     * {@code Parada.distanciaKm}. O filtro por caixa existe para que o banco nao
     * precise percorrer as 22 mil paradas da cidade a cada consulta do aplicativo,
     * aproveitando o indice composto de latitude e longitude.</p>
     */
    @Query("""
            SELECT p FROM Parada p
            WHERE p.latitude BETWEEN :latMin AND :latMax
              AND p.longitude BETWEEN :lonMin AND :lonMax
            """)
    List<Parada> buscarNaCaixa(@Param("latMin") double latMin,
                               @Param("latMax") double latMax,
                               @Param("lonMin") double lonMin,
                               @Param("lonMax") double lonMax);

    /** Paradas servidas por uma linha, na ordem em que o onibus passa. */
    @Query("""
            SELECT i.parada FROM Itinerario i
            WHERE i.linha.id = :linhaId
            ORDER BY i.ordem ASC
            """)
    List<Parada> buscarPorLinha(@Param("linhaId") Long linhaId);
}
