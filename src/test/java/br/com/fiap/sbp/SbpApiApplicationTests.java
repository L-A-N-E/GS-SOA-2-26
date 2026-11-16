package br.com.fiap.sbp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Garante que o contexto do Spring sobe por inteiro: entidades mapeadas,
 * repositorios, servicos e controladores. E o teste que pega erro de
 * configuracao antes que ele apareca no ambiente de verdade.
 */
@SpringBootTest
@ActiveProfiles("test")
class SbpApiApplicationTests {

    @Test
    @DisplayName("O contexto da aplicação carrega sem erros")
    void contextoCarrega() {
        // Se chegou ate aqui, todo o grafo de beans foi construido com sucesso.
    }
}
