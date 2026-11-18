package br.com.fiap.sbp.domain.repository;

import br.com.fiap.sbp.domain.entity.Linha;
import br.com.fiap.sbp.domain.enums.AreaOperacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LinhaRepository extends JpaRepository<Linha, Long> {

    Optional<Linha> findByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);

    Page<Linha> findByAreaOperacao(AreaOperacao areaOperacao, Pageable paginacao);

    /** Busca unificada do aplicativo: aceita o número da linha ou parte do nome. */
    @Query("""
            SELECT l FROM Linha l
            WHERE UPPER(l.codigo) LIKE UPPER(CONCAT('%', :termo, '%'))
               OR UPPER(l.nome)   LIKE UPPER(CONCAT('%', :termo, '%'))
            """)
    Page<Linha> buscarPorTermo(@Param("termo") String termo, Pageable paginacao);

    /** Linhas que servem uma parada, usadas para montar o painel permanente do totem. */
    @Query("""
            SELECT i.linha FROM Itinerario i
            WHERE i.parada.id = :paradaId
            ORDER BY i.linha.codigo ASC
            """)
    List<Linha> buscarPorParada(@Param("paradaId") Long paradaId);
}
