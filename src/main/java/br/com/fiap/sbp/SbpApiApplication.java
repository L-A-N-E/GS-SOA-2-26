package br.com.fiap.sbp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * S.B.P. — Smart Bus Point.
 *
 * <p>Backend da CitizenTech proposta pela equipe para a Global Solution 2026.2.
 * Centraliza as informacoes consumidas pelo totem da calcada, pelo aplicativo e
 * pelo site, de forma que o cidadao tenha a mesma informacao em qualquer um dos
 * tres — inclusive quando nao tem celular, bateria ou dados moveis.</p>
 */
@SpringBootApplication
public class SbpApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SbpApiApplication.class, args);
    }
}
