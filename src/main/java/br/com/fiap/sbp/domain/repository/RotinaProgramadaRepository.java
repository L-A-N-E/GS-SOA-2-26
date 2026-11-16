package br.com.fiap.sbp.domain.repository;

import br.com.fiap.sbp.domain.entity.RotinaProgramada;
import br.com.fiap.sbp.domain.enums.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RotinaProgramadaRepository extends JpaRepository<RotinaProgramada, Long> {

    List<RotinaProgramada> findByCidadaoId(Long cidadaoId);

    /**
     * Rotinas ativas do cidadao para um dia da semana.
     * Alimenta o card Rota de Hoje da tela inicial do aplicativo.
     */
    @Query("""
            SELECT r FROM RotinaProgramada r
            JOIN FETCH r.linha
            JOIN FETCH r.paradaEmbarque
            WHERE r.cidadao.id = :cidadaoId
              AND r.ativa = true
              AND :dia MEMBER OF r.dias
            ORDER BY r.horarioHabitual ASC
            """)
    List<RotinaProgramada> buscarAtivasNoDia(@Param("cidadaoId") Long cidadaoId,
                                             @Param("dia") DiaSemana dia);
}
