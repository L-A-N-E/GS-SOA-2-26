package br.com.fiap.sbp.domain;

import br.com.fiap.sbp.domain.entity.Linha;
import br.com.fiap.sbp.domain.entity.Parada;
import br.com.fiap.sbp.domain.entity.PrevisaoChegada;
import br.com.fiap.sbp.domain.entity.Totem;
import br.com.fiap.sbp.domain.enums.OrigemPrevisao;
import br.com.fiap.sbp.domain.enums.StatusTotem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa as regras que vivem dentro das entidades.
 *
 * <p>São as decisões que o cidadão vê na tela: se o ônibus já está no ponto, se
 * a linha entra no carrossel de emergência, o que o totem fala em voz alta e
 * quando a operação precisa ser avisada de um equipamento sem comunicação.</p>
 */
class DominioTest {

    @Nested
    @DisplayName("Parada")
    class ParadaTest {

        @Test
        @DisplayName("Calcula a distância entre duas coordenadas conhecidas de São Paulo")
        void calculaDistancia() {
            // Praça da Sé -> MASP: cerca de 2,7 km em linha reta
            Parada se = Parada.builder()
                    .nome("Praça da Sé").latitude(-23.5505).longitude(-46.6333).build();

            double distancia = se.distanciaKm(-23.561414, -46.655881);

            assertThat(distancia).isBetween(2.4, 3.0);
        }

        @Test
        @DisplayName("A distância até a própria coordenada e zero")
        void distanciaParaSiMesma() {
            Parada parada = Parada.builder().latitude(-23.5505).longitude(-46.6333).build();

            assertThat(parada.distanciaKm(-23.5505, -46.6333)).isZero();
        }

        @Test
        @DisplayName("A descrição acessível inclui a referência urbana quando existe")
        void descricaoAcessivelComReferencia() {
            Parada parada = Parada.builder()
                    .nome("Av. Paulista, 1578").referenciaUrbana("Em frente ao MASP").build();

            assertThat(parada.descricaoAcessivel())
                    .isEqualTo("Parada Av. Paulista, 1578, Em frente ao MASP");
        }

        @Test
        @DisplayName("Sem referência urbana, a descrição acessível usa so o nome")
        void descricaoAcessivelSemReferencia() {
            Parada parada = Parada.builder().nome("R. Tadao Inoue, 8").referenciaUrbana("  ").build();

            assertThat(parada.descricaoAcessivel()).isEqualTo("Parada R. Tadao Inoue, 8");
        }

        @Test
        @DisplayName("Uma parada sem totem não tem totem ativo")
        void paradaSemTotem() {
            assertThat(Parada.builder().nome("Qualquer").build().possuiTotemAtivo()).isFalse();
        }
    }

    @Nested
    @DisplayName("Linha")
    class LinhaTest {

        @ParameterizedTest(name = "a linha {0} e noturna? {1}")
        @CsvSource({"N101-11, true", "n140-10, true", "8000-10, false", "1012-10, false"})
        @DisplayName("Reconhece as linhas noturnas do Corujão pelo prefixo N")
        void reconheceLinhaNoturna(String codigo, boolean esperado) {
            assertThat(Linha.builder().codigo(codigo).build().noturna()).isEqualTo(esperado);
        }

        @Test
        @DisplayName("Calcula a velocidade média programada")
        void calculaVelocidadeMedia() {
            Linha linha = Linha.builder().extensaoKm(18.0).duracaoProgramadaMin(72).build();

            assertThat(linha.velocidadeMediaKmh()).isEqualTo(15.0);
        }

        @Test
        @DisplayName("Sem duração programada, não há velocidade média a calcular")
        void semDuracaoNaoCalculaVelocidade() {
            assertThat(Linha.builder().extensaoKm(18.0).build().velocidadeMediaKmh()).isNull();
        }
    }

    @Nested
    @DisplayName("PrevisaoChegada")
    class PrevisaoTest {

        private PrevisaoChegada previsaoDe(int minutos, OrigemPrevisao origem) {
            return PrevisaoChegada.builder()
                    .linha(Linha.builder().codigo("8000-10").build())
                    .minutosEstimados(minutos)
                    .origem(origem)
                    .geradaEm(LocalDateTime.now())
                    .build();
        }

        @Test
        @DisplayName("Zero minuto significa que o ônibus já está no ponto")
        void onibusNoPonto() {
            PrevisaoChegada previsao = previsaoDe(0, OrigemPrevisao.TEMPO_REAL);

            assertThat(previsao.onibusNoPonto()).isTrue();
            assertThat(previsao.textoAcessivel())
                    .isEqualTo("Atenção. O ônibus da linha 8000-10 está no ponto agora.");
        }

        @ParameterizedTest(name = "faltando {0} min, entra no carrossel de emergência? {1}")
        @CsvSource({"0, true", "1, true", "2, true", "3, false", "7, false"})
        @DisplayName("So entra no carrossel de emergência quem esta a menos de 3 minutos")
        void carrosselDeEmergencia(int minutos, boolean esperado) {
            assertThat(previsaoDe(minutos, OrigemPrevisao.TEMPO_REAL).emergencia())
                    .isEqualTo(esperado);
        }

        @Test
        @DisplayName("O texto falado usa o singular quando falta 1 minuto")
        void textoNoSingular() {
            assertThat(previsaoDe(1, OrigemPrevisao.TEMPO_REAL).textoAcessivel())
                    .isEqualTo("O ônibus da linha 8000-10 chega em 1 minuto.");
        }

        @Test
        @DisplayName("O texto falado avisa quando o horário é uma estimativa do modelo")
        void textoAvisaQueEEstimativa() {
            assertThat(previsaoDe(7, OrigemPrevisao.MODELO_ML).textoAcessivel())
                    .isEqualTo("O ônibus da linha 8000-10 chega em 7 minutos. "
                            + "Este horário é uma estimativa.");
        }

        @Test
        @DisplayName("Previsão vinda da telemetria não e anunciada como estimativa")
        void tempoRealNaoEEstimativa() {
            assertThat(previsaoDe(7, OrigemPrevisao.TEMPO_REAL).textoAcessivel())
                    .doesNotContain("estimativa");
        }

        @Test
        @DisplayName("Com faixa, o texto falado anuncia o intervalo em vez do número")
        void textoAnunciaFaixa() {
            PrevisaoChegada previsao = previsaoDe(14, OrigemPrevisao.MODELO_ML);
            previsao.setFaixaMinMinutos(8);
            previsao.setFaixaMaxMinutos(25);

            assertThat(previsao.possuiFaixa()).isTrue();
            assertThat(previsao.faixaFormatada()).isEqualTo("8 a 25 min");
            assertThat(previsao.textoAcessivel())
                    .isEqualTo("O ônibus da linha 8000-10 deve chegar entre 8 e 25 minutos. "
                            + "Este horário é uma estimativa.");
        }

        @Test
        @DisplayName("Sem faixa, a previsão estimada continua anunciando o número")
        void semFaixaAnunciaNumero() {
            PrevisaoChegada previsao = previsaoDe(14, OrigemPrevisao.MODELO_ML);

            assertThat(previsao.possuiFaixa()).isFalse();
            assertThat(previsao.faixaFormatada()).isNull();
            assertThat(previsao.textoAcessivel()).contains("chega em 14 minutos");
        }

        @Test
        @DisplayName("A faixa não muda o anuncio de ônibus que já está no ponto")
        void faixaNaoAfetaOnibusNoPonto() {
            PrevisaoChegada previsao = previsaoDe(0, OrigemPrevisao.TEMPO_REAL);
            previsao.setFaixaMinMinutos(0);
            previsao.setFaixaMaxMinutos(4);

            assertThat(previsao.textoAcessivel()).isEqualTo(
                    "Atenção. O ônibus da linha 8000-10 está no ponto agora.");
        }

        @Test
        @DisplayName("Previsão com mais de 5 minutos de vida está expirada")
        void previsaoExpirada() {
            PrevisaoChegada velha = previsaoDe(5, OrigemPrevisao.TEMPO_REAL);
            velha.setGeradaEm(LocalDateTime.now().minusMinutes(6));

            assertThat(velha.expirada()).isTrue();
            assertThat(previsaoDe(5, OrigemPrevisao.TEMPO_REAL).expirada()).isFalse();
        }
    }

    @Nested
    @DisplayName("Totem")
    class TotemTest {

        @Test
        @DisplayName("Um totem ativo que parou de sincronizar precisa de atenção")
        void totemMudoPrecisaAtencao() {
            Totem totem = Totem.builder().codigo("SBP-0001").status(StatusTotem.ATIVO)
                    .ultimaSincronizacao(LocalDateTime.now().minusMinutes(40)).build();

            assertThat(totem.precisaAtencao()).isTrue();
        }

        @Test
        @DisplayName("Um totem que acabou de sincronizar está em ordem")
        void totemSincronizadoNaoPrecisaAtencao() {
            Totem totem = Totem.builder().codigo("SBP-0001").status(StatusTotem.ATIVO)
                    .ultimaSincronizacao(LocalDateTime.now().minusMinutes(2)).build();

            assertThat(totem.precisaAtencao()).isFalse();
        }

        @Test
        @DisplayName("Sincronizar traz de volta um totem que estava sem comunicação")
        void sincronizarReativaTotem() {
            Totem totem = Totem.builder().codigo("SBP-0001")
                    .status(StatusTotem.SEM_COMUNICACAO).build();

            totem.sincronizar();

            assertThat(totem.getStatus()).isEqualTo(StatusTotem.ATIVO);
            assertThat(totem.getUltimaSincronizacao()).isNotNull();
            assertThat(totem.precisaAtencao()).isFalse();
        }

        @Test
        @DisplayName("Sincronizar não tira da manutenção um totem que a equipe desligou")
        void sincronizarNaoBurlaManutencao() {
            Totem totem = Totem.builder().codigo("SBP-0003")
                    .status(StatusTotem.MANUTENCAO).build();

            totem.sincronizar();

            assertThat(totem.getStatus()).isEqualTo(StatusTotem.MANUTENCAO);
        }

        @Test
        @DisplayName("So e plenamente acessível o totem com voz, Braille e parada adaptada")
        void acessibilidadeCompleta() {
            Parada adaptada = Parada.builder().nome("Terminal").acessivelCadeirante(true).build();
            Parada naoAdaptada = Parada.builder().nome("Ponto").acessivelCadeirante(false).build();

            assertThat(Totem.builder().parada(adaptada)
                    .possuiAudioTts(true).possuiBraille(true).build()
                    .plenamenteAcessivel()).isTrue();

            assertThat(Totem.builder().parada(adaptada)
                    .possuiAudioTts(true).possuiBraille(false).build()
                    .plenamenteAcessivel()).isFalse();

            assertThat(Totem.builder().parada(naoAdaptada)
                    .possuiAudioTts(true).possuiBraille(true).build()
                    .plenamenteAcessivel()).isFalse();
        }
    }
}
