package br.com.fiap.sbp.domain.repository;

import br.com.fiap.sbp.domain.entity.PrevisaoChegada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PrevisaoChegadaRepository extends JpaRepository<PrevisaoChegada, Long> {

    /**
     * Previsões vigentes de uma parada, da mais próxima para a mais distante.
     * É a consulta que monta o painel do totem e a tela do ponto no aplicativo.
     */
    @Query("""
            SELECT pr FROM PrevisaoChegada pr
            JOIN FETCH pr.linha
            WHERE pr.parada.id = :paradaId
              AND pr.geradaEm >= :desde
            ORDER BY pr.minutosEstimados ASC
            """)
    List<PrevisaoChegada> buscarVigentesPorParada(@Param("paradaId") Long paradaId,
                                                  @Param("desde") LocalDateTime desde);

    @Query("""
            SELECT pr FROM PrevisaoChegada pr
            WHERE pr.parada.id = :paradaId
              AND pr.linha.id = :linhaId
              AND pr.geradaEm >= :desde
            ORDER BY pr.minutosEstimados ASC
            """)
    List<PrevisaoChegada> buscarVigentesPorParadaELinha(@Param("paradaId") Long paradaId,
                                                        @Param("linhaId") Long linhaId,
                                                        @Param("desde") LocalDateTime desde);

    /** Limpeza das previsões que já não podem ser exibidas ao cidadão. */
    @Modifying
    @Query("DELETE FROM PrevisaoChegada pr WHERE pr.geradaEm < :limite")
    int removerAnterioresA(@Param("limite") LocalDateTime limite);
}
