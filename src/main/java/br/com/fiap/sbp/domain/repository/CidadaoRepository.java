package br.com.fiap.sbp.domain.repository;

import br.com.fiap.sbp.domain.entity.Cidadao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CidadaoRepository extends JpaRepository<Cidadao, Long> {

    Optional<Cidadao> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
