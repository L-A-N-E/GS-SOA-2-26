package br.com.fiap.sbp.domain.repository;

import br.com.fiap.sbp.domain.entity.Itinerario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItinerarioRepository extends JpaRepository<Itinerario, Long> {

    /** Trajeto completo de uma linha, na ordem em que o ônibus percorre as paradas. */
    @Query("""
            SELECT i FROM Itinerario i
            JOIN FETCH i.parada
            WHERE i.linha.id = :linhaId
            ORDER BY i.ordem ASC
            """)
    List<Itinerario> buscarTrajetoDaLinha(@Param("linhaId") Long linhaId);

    Optional<Itinerario> findByLinhaIdAndParadaId(Long linhaId, Long paradaId);

    boolean existsByLinhaIdAndOrdem(Long linhaId, Integer ordem);
}
