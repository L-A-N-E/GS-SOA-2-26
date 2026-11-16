package br.com.fiap.sbp.domain.repository;

import br.com.fiap.sbp.domain.entity.PontoDeInteresse;
import br.com.fiap.sbp.domain.enums.CategoriaPontoInteresse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PontoDeInteresseRepository extends JpaRepository<PontoDeInteresse, Long> {

    Page<PontoDeInteresse> findByCategoria(CategoriaPontoInteresse categoria, Pageable paginacao);

    Page<PontoDeInteresse> findByNomeContainingIgnoreCase(String nome, Pageable paginacao);

    /** Pre-filtro por caixa de coordenadas, com a distancia exata calculada depois. */
    @Query("""
            SELECT poi FROM PontoDeInteresse poi
            WHERE poi.latitude BETWEEN :latMin AND :latMax
              AND poi.longitude BETWEEN :lonMin AND :lonMax
            """)
    List<PontoDeInteresse> buscarNaCaixa(@Param("latMin") double latMin,
                                         @Param("latMax") double latMax,
                                         @Param("lonMin") double lonMin,
                                         @Param("lonMax") double lonMax);
}
