package br.com.fiap.sbp.domain.repository;

import br.com.fiap.sbp.domain.entity.Totem;
import br.com.fiap.sbp.domain.enums.StatusTotem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TotemRepository extends JpaRepository<Totem, Long> {

    Optional<Totem> findByCodigoIgnoreCase(String codigo);

    /** Usado quando o cidadao le o QR Code do totem com o celular. */
    Optional<Totem> findByTokenQrCode(String tokenQrCode);

    boolean existsByCodigoIgnoreCase(String codigo);

    Optional<Totem> findByParadaId(Long paradaId);

    Page<Totem> findByStatus(StatusTotem status, Pageable paginacao);

    /**
     * Totens que deveriam estar operando mas pararam de sincronizar.
     * Alimenta o painel de monitoramento da operacao.
     */
    @Query("""
            SELECT t FROM Totem t
            WHERE t.status = br.com.fiap.sbp.domain.enums.StatusTotem.ATIVO
              AND (t.ultimaSincronizacao IS NULL OR t.ultimaSincronizacao < :limite)
            """)
    List<Totem> buscarSemComunicacao(@Param("limite") LocalDateTime limite);

    long countByStatus(StatusTotem status);
}
