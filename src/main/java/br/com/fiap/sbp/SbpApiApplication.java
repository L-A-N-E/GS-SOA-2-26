package br.com.fiap.sbp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * S.B.P. — Smart Bus Point.
 *
 * <p>Backend da CitizenTech proposta pela equipe para a Global Solution 2026.2.
 * Centraliza as informações consumidas pelo totem da calçada, pelo aplicativo e
 * pelo site, de modo que o cidadão tenha a mesma informação em qualquer um dos
 * três — inclusive quando não tem celular, bateria ou dados móveis.</p>
 */
@SpringBootApplication
public class SbpApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SbpApiApplication.class, args);
    }
}
