package br.com.fiap.sbp.service;

import br.com.fiap.sbp.domain.entity.Linha;
import br.com.fiap.sbp.domain.entity.Parada;
import br.com.fiap.sbp.domain.entity.PrevisaoChegada;
import br.com.fiap.sbp.domain.enums.NivelLotacao;
import br.com.fiap.sbp.domain.enums.OrigemPrevisao;
import br.com.fiap.sbp.domain.repository.PrevisaoChegadaRepository;
import br.com.fiap.sbp.exception.RegraDeNegocioException;
import br.com.fiap.sbp.web.dto.PrevisaoRequest;
import br.com.fiap.sbp.web.dto.PrevisaoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Testa a regra que protege a faixa de espera exibida ao cidadão.
 *
 * <p>Uma faixa pela metade, invertida ou que não contém a própria estimativa
 * apareceria no totem como se fosse confiável. Recusar a previsão é melhor do
 * que publicar um intervalo sem sentido na calçada.</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PrevisaoServiceTest {

    @Mock private PrevisaoChegadaRepository previsaoRepository;
    @Mock private LinhaService linhaService;
    @Mock private ParadaService paradaService;

    private PrevisaoService previsaoService;

    @BeforeEach
    void preparar() {
        previsaoService = new PrevisaoService(previsaoRepository, linhaService, paradaService);
        when(linhaService.buscarEntidade(anyLong()))
                .thenReturn(Linha.builder().id(1L).codigo("1177-10").nome("Terminal - Luz").build());
        when(paradaService.buscarEntidade(anyLong()))
                .thenReturn(Parada.builder().id(1L).nome("Av. Prestes Maia, 940").build());
        when(previsaoRepository.save(any(PrevisaoChegada.class)))
                .thenAnswer(chamada -> chamada.getArgument(0));
    }

    private PrevisaoRequest requisicao(Integer minutos, Integer faixaMin, Integer faixaMax) {
        return new PrevisaoRequest(1L, 1L, minutos, OrigemPrevisao.MODELO_ML,
                NivelLotacao.DESCONHECIDO, null, faixaMin, faixaMax);
    }

    @Test
    @DisplayName("Registra a previsão com a faixa e monta o texto falado com o intervalo")
    void registraComFaixa() {
        PrevisaoResponse resposta = previsaoService.registrar(requisicao(14, 8, 25));

        assertThat(resposta.faixaMinMinutos()).isEqualTo(8);
        assertThat(resposta.faixaMaxMinutos()).isEqualTo(25);
        assertThat(resposta.faixaFormatada()).isEqualTo("8 a 25 min");
        assertThat(resposta.textoAcessivel()).contains("deve chegar entre 8 e 25 minutos");
    }

    @Test
    @DisplayName("Previsão pontual continua valida, sem faixa")
    void registraSemFaixa() {
        PrevisaoResponse resposta = previsaoService.registrar(
                new PrevisaoRequest(1L, 1L, 7, OrigemPrevisao.TEMPO_REAL,
                        NivelLotacao.VAZIO, "31207", null, null));

        assertThat(resposta.faixaFormatada()).isNull();
        assertThat(resposta.textoAcessivel()).contains("chega em 7 minutos");
    }

    @Test
    @DisplayName("Recusa uma faixa pela metade")
    void recusaFaixaIncompleta() {
        assertThatThrownBy(() -> previsaoService.registrar(requisicao(14, 8, null)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("os dois limites");

        verify(previsaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Recusa uma faixa invertida")
    void recusaFaixaInvertida() {
        assertThatThrownBy(() -> previsaoService.registrar(requisicao(14, 25, 8)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("não pode ser maior");
    }

    @Test
    @DisplayName("Recusa uma estimativa que cai fora da própria faixa")
    void recusaEstimativaForaDaFaixa() {
        assertThatThrownBy(() -> previsaoService.registrar(requisicao(40, 8, 25)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("fora da própria faixa");
    }

    @Test
    @DisplayName("Recusa previsão longa demais para ser útil no ponto")
    void recusaPrevisaoLongaDemais() {
        assertThatThrownBy(() -> previsaoService.registrar(requisicao(200, null, null)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("não é útil");
    }

    @Test
    @DisplayName("Aceita a estimativa exatamente nos extremos da faixa")
    void aceitaEstimativaNosExtremos() {
        assertThat(previsaoService.registrar(requisicao(8, 8, 25)).faixaFormatada())
                .isEqualTo("8 a 25 min");
        assertThat(previsaoService.registrar(requisicao(25, 8, 25)).faixaFormatada())
                .isEqualTo("8 a 25 min");
    }
}
