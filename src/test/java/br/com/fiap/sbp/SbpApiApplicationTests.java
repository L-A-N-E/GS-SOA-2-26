package br.com.fiap.sbp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Garante que o contexto do Spring sobe por inteiro: entidades mapeadas,
 * repositórios, serviços e controladores. É o teste que detecta erro de
 * configuração antes que ele apareça no ambiente real.
 */
@SpringBootTest
@ActiveProfiles("test")
class SbpApiApplicationTests {

    @Test
    @DisplayName("O contexto da aplicação carrega sem erros")
    void contextoCarrega() {
        // Se chegou até aqui, todo o grafo de beans foi construído com sucesso.
    }
}
