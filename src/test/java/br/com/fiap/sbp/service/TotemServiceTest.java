package br.com.fiap.sbp.service;

import br.com.fiap.sbp.domain.entity.Linha;
import br.com.fiap.sbp.domain.entity.Parada;
import br.com.fiap.sbp.domain.entity.PrevisaoChegada;
import br.com.fiap.sbp.domain.entity.Totem;
import br.com.fiap.sbp.domain.enums.OrigemPrevisao;
import br.com.fiap.sbp.domain.enums.StatusTotem;
import br.com.fiap.sbp.domain.repository.TotemRepository;
import br.com.fiap.sbp.exception.RegraDeNegocioException;
import br.com.fiap.sbp.web.dto.PainelTotemResponse;
import br.com.fiap.sbp.web.dto.TotemRequest;
import br.com.fiap.sbp.web.dto.TotemResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testa o endpoint central do S.B.P.: a montagem do painel do totem.
 *
 * <p>O que importa aqui é o que o cidadão vê na calçada — a ordem das previsões,
 * o destaque do carrossel, o texto que o botão tátil lê em voz alta — e a regra
 * que impede um totem fora de operação de exibir informação.</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TotemServiceTest {

    @Mock private TotemRepository totemRepository;
    @Mock private ParadaService paradaService;
    @Mock private PrevisaoService previsaoService;
    @Mock private PontoInteresseService pontoInteresseService;

    private TotemService totemService;

    private Parada parada;
    private Linha linha;

    @BeforeEach
    void preparar() {
        totemService = new TotemService(totemRepository, paradaService,
                previsaoService, pontoInteresseService, "https://sbp.mobi/p/");

        parada = Parada.builder().id(1L).codigoGtfs("301790")
                .nome("Terminal Jardim Britania")
                .referenciaUrbana("Plataforma de embarque")
                .latitude(-23.431924).longitude(-46.787131)
                .acessivelCadeirante(true).build();

        linha = Linha.builder().id(1L).codigo("1012-10")
                .nome("Term. Jd. Britania - Jd. Monte Belo").build();

        when(pontoInteresseService.buscarProximasDaParada(anyLong(), anyDouble(), anyInt()))
                .thenReturn(List.of());
        when(totemRepository.save(any(Totem.class))).thenAnswer(c -> c.getArgument(0));
    }

    private Totem totemAtivo() {
        return Totem.builder().id(1L).codigo("SBP-0001").parada(parada)
                .status(StatusTotem.ATIVO).tokenQrCode("sbp-a1b2c3d4e5f6")
                .possuiAudioTts(true).possuiBraille(true)
                .ultimaSincronizacao(LocalDateTime.now().minusMinutes(1)).build();
    }

    private PrevisaoChegada previsaoDe(int minutos, OrigemPrevisao origem) {
        return PrevisaoChegada.builder().linha(linha).parada(parada)
                .minutosEstimados(minutos).origem(origem)
                .geradaEm(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("Monta o painel com a parada, as previsões e o endereço do QR Code")
    void montaPainelCompleto() {
        when(totemRepository.findByCodigoIgnoreCase("SBP-0001"))
                .thenReturn(Optional.of(totemAtivo()));
        when(previsaoService.buscarEntidadesVigentes(1L))
                .thenReturn(List.of(previsaoDe(2, OrigemPrevisao.TEMPO_REAL),
                        previsaoDe(18, OrigemPrevisao.GTFS_PROGRAMADO)));
        when(previsaoService.buscarDestaqueDoCarrossel(1L))
                .thenReturn(Optional.of(previsaoDe(2, OrigemPrevisao.TEMPO_REAL)));

        PainelTotemResponse painel = totemService.montarPainel("SBP-0001");

        assertThat(painel.codigoTotem()).isEqualTo("SBP-0001");
        assertThat(painel.parada().nome()).isEqualTo("Terminal Jardim Britania");
        assertThat(painel.urlQrCode()).isEqualTo("https://sbp.mobi/p/sbp-a1b2c3d4e5f6");
        assertThat(painel.previsoes()).hasSize(2);
        assertThat(painel.destaqueCarrossel()).isNotNull();
        assertThat(painel.destaqueCarrossel().emergencia()).isTrue();
    }

    @Test
    @DisplayName("O texto do botão tátil anuncia a parada e as próximas chegadas")
    void montaTextoAcessivel() {
        when(totemRepository.findByCodigoIgnoreCase("SBP-0001"))
                .thenReturn(Optional.of(totemAtivo()));
        when(previsaoService.buscarEntidadesVigentes(1L))
                .thenReturn(List.of(previsaoDe(2, OrigemPrevisao.TEMPO_REAL)));
        when(previsaoService.buscarDestaqueDoCarrossel(1L)).thenReturn(Optional.empty());

        PainelTotemResponse painel = totemService.montarPainel("SBP-0001");

        assertThat(painel.textoAcessivelCompleto())
                .startsWith("Parada Terminal Jardim Britania, Plataforma de embarque.")
                .contains("linha 1012-10")
                .contains("2 minutos");
    }

    @Test
    @DisplayName("Sem previsão disponível, o totem diz isso em voz alta em vez de ficar mudo")
    void avisaQuandoNaoHaPrevisao() {
        when(totemRepository.findByCodigoIgnoreCase("SBP-0001"))
                .thenReturn(Optional.of(totemAtivo()));
        when(previsaoService.buscarEntidadesVigentes(1L)).thenReturn(List.of());
        when(previsaoService.buscarDestaqueDoCarrossel(1L)).thenReturn(Optional.empty());

        PainelTotemResponse painel = totemService.montarPainel("SBP-0001");

        assertThat(painel.previsoes()).isEmpty();
        assertThat(painel.destaqueCarrossel()).isNull();
        assertThat(painel.textoAcessivelCompleto())
                .contains("não há previsão de chegada disponível");
    }

    @Test
    @DisplayName("Um totem em manutenção não exibe painel")
    void totemEmManutencaoNaoExibePainel() {
        Totem emManutencao = totemAtivo();
        emManutencao.setStatus(StatusTotem.MANUTENCAO);
        when(totemRepository.findByCodigoIgnoreCase("SBP-0003"))
                .thenReturn(Optional.of(emManutencao));

        assertThatThrownBy(() -> totemService.montarPainel("SBP-0003"))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("não está em operação");
    }

    @Test
    @DisplayName("Consultar o painel registra o batimento do totem")
    void consultarPainelRegistraBatimento() {
        Totem totem = totemAtivo();
        totem.setUltimaSincronizacao(LocalDateTime.now().minusHours(2));
        when(totemRepository.findByCodigoIgnoreCase("SBP-0001")).thenReturn(Optional.of(totem));
        when(previsaoService.buscarEntidadesVigentes(1L)).thenReturn(List.of());
        when(previsaoService.buscarDestaqueDoCarrossel(1L)).thenReturn(Optional.empty());

        totemService.montarPainel("SBP-0001");

        assertThat(totem.minutosDesdeUltimaSincronizacao()).isLessThan(1);
        verify(totemRepository).save(totem);
    }

    @Test
    @DisplayName("Não instala um segundo totem em uma parada que já tem um")
    void naoInstalaDoisTotensNaMesmaParada() {
        TotemRequest requisicao = new TotemRequest("SBP-0009", 1L, StatusTotem.ATIVO,
                "1.4.2", true, true);
        when(totemRepository.existsByCodigoIgnoreCase("SBP-0009")).thenReturn(false);
        when(paradaService.buscarEntidade(1L)).thenReturn(parada);
        when(totemRepository.findByParadaId(1L))
                .thenReturn(Optional.of(Totem.builder().codigo("SBP-0001").build()));

        assertThatThrownBy(() -> totemService.instalar(requisicao))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("SBP-0001");
    }

    @Test
    @DisplayName("Ao instalar, gera automaticamente o token do QR Code")
    void geraTokenDoQrCodeNaInstalacao() {
        TotemRequest requisicao = new TotemRequest("SBP-0009", 1L, null, "1.4.2", true, true);
        when(totemRepository.existsByCodigoIgnoreCase("SBP-0009")).thenReturn(false);
        when(paradaService.buscarEntidade(1L)).thenReturn(parada);
        when(totemRepository.findByParadaId(1L)).thenReturn(Optional.empty());

        TotemResponse instalado = totemService.instalar(requisicao);

        assertThat(instalado.tokenQrCode()).startsWith("sbp-").hasSize(16);
        assertThat(instalado.status()).isEqualTo(StatusTotem.ATIVO);
        assertThat(instalado.plenamenteAcessivel()).isTrue();
    }
}
