package br.com.fiap.sbp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Documentação automática da API, em OpenAPI 3 com Swagger UI.
 *
 * <p>A documentação é gerada a partir do próprio código pelo springdoc: as
 * anotações nos controladores e nos DTOs produzem a especificação, que assim
 * não envelhece em relação à implementação.</p>
 *
 * <ul>
 * <li>Interface: <a href="http://localhost:8080/swagger-ui.html">/swagger-ui.html</a></li>
 * <li>Especificação: <a href="http://localhost:8080/v3/api-docs">/v3/api-docs</a></li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApiDoSbp() {
        return new OpenAPI()
                .info(new Info()
                        .title("S.B.P. — Smart Bus Point API")
                        .version("1.0.0")
                        .description("""
                                Backend da CitizenTech **S.B.P. (Smart Bus Point)**, desenvolvido para a
                                Global Solution 2026.2 da FIAP — Engenharia de Software, 3º ano de Fev.

                                ### O problema
                                O cidadão que espera no ponto de ônibus não sabe quanto tempo ainda vai
                                esperar. Mais da metade dos passageiros aponta o tempo de espera como o
                                principal motivo de insatisfação com o sistema, e essa incerteza empurra
                                quem depende do ônibus para modais individuais.

                                ### A solução
                                Um totem na calçada, um aplicativo e um site, ligados por um QR Code.
                                Esta API é o que sustenta os três: ela entrega a previsão de chegada, o
                                trajeto da linha, as referências urbanas do bairro e o texto que o botão
                                tátil do totem lê em voz alta para pessoas com deficiência visual.

                                ### Por onde começar
                                - `GET /api/totens/{codigo}/painel` — a tela inteira do totem em uma chamada
                                - `GET /api/totens/qrcode/{token}` — o mesmo painel, a partir do QR Code lido no ponto
                                - `GET /api/paradas/proximas` — qual é o ponto mais perto de mim
                                - `GET /api/cidadaos/{id}/rota-de-hoje` — o card Rota de Hoje do aplicativo

                                ### Dados
                                A base é carregada com dados reais e públicos do GTFS da SPTrans
                                (https://www.sptrans.com.br/desenvolvedores/). Os cadastros de cidadãos
                                são fictícios: nenhum dado pessoal real é utilizado.
                                """)
                        .contact(new Contact()
                                .name("Equipe S.B.P. — FIAP 3ESF")
                                .url("https://github.com/"))
                        .license(new License()
                                .name("Uso acadêmico — Global Solution FIAP 2026.2")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Ambiente local")));
    }
}
