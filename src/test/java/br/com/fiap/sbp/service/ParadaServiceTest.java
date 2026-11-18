package br.com.fiap.sbp.service;

import br.com.fiap.sbp.domain.entity.Parada;
import br.com.fiap.sbp.domain.entity.Totem;
import br.com.fiap.sbp.domain.repository.ItinerarioRepository;
import br.com.fiap.sbp.domain.repository.ParadaRepository;
import br.com.fiap.sbp.exception.RecursoNaoEncontradoException;
import br.com.fiap.sbp.exception.RegraDeNegocioException;
import br.com.fiap.sbp.web.dto.ParadaProximaResponse;
import br.com.fiap.sbp.web.dto.ParadaRequest;
import br.com.fiap.sbp.web.dto.ParadaResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testa as regras de negócio das paradas sem subir banco nem contexto Spring:
 * os repositórios são simulados, de modo que o teste exercita apenas a decisão
 * que o serviço toma.
 */
@ExtendWith(MockitoExtension.class)
class ParadaServiceTest {

    @Mock
    private ParadaRepository paradaRepository;

    @Mock
    private ItinerarioRepository itinerarioRepository;

    @InjectMocks
    private ParadaService paradaService;

    private Parada paradaDe(String codigo, String nome, double lat, double lon) {
        return Parada.builder().id(1L).codigoGtfs(codigo).nome(nome)
                .latitude(lat).longitude(lon).build();
    }

    @Test
    @DisplayName("Buscar uma parada inexistente resulta em recurso não encontrado")
    void buscarParadaInexistente() {
        when(paradaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paradaService.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Parada")
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Não permite cadastrar duas paradas com o mesmo código GTFS")
    void naoPermiteCodigoGtfsRepetido() {
        ParadaRequest requisicao = new ParadaRequest("340015890", "Av. Paulista, 1578",
                "Em frente ao MASP", -23.561414, -46.655881, true, true);
        when(paradaRepository.existsByCodigoGtfs("340015890")).thenReturn(true);

        assertThatThrownBy(() -> paradaService.criar(requisicao))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("340015890");

        verify(paradaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cadastra a parada quando o código GTFS esta livre")
    void cadastraParada() {
        ParadaRequest requisicao = new ParadaRequest("340015890", "Av. Paulista, 1578",
                "Em frente ao MASP", -23.561414, -46.655881, true, true);
        when(paradaRepository.existsByCodigoGtfs("340015890")).thenReturn(false);
        when(paradaRepository.save(any(Parada.class)))
                .thenAnswer(chamada -> {
                    Parada salva = chamada.getArgument(0);
                    salva.setId(1L);
                    return salva;
                });

        ParadaResponse resposta = paradaService.criar(requisicao);

        assertThat(resposta.id()).isEqualTo(1L);
        assertThat(resposta.codigoGtfs()).isEqualTo("340015890");
        assertThat(resposta.descricaoAcessivel())
                .isEqualTo("Parada Av. Paulista, 1578, Em frente ao MASP");
    }

    @Test
    @DisplayName("Não remove uma parada que ainda tem totem instalado")
    void naoRemoveParadaComTotem() {
        Parada parada = paradaDe("301790", "Terminal Jardim Britania", -23.431924, -46.787131);
        parada.setTotem(Totem.builder().codigo("SBP-0001").build());
        when(paradaRepository.findById(1L)).thenReturn(Optional.of(parada));

        assertThatThrownBy(() -> paradaService.remover(1L))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("SBP-0001");

        verify(paradaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Rejeita um raio de busca maior que o limite de 5 km")
    void rejeitaRaioForaDoLimite() {
        assertThatThrownBy(() -> paradaService.buscarProximas(-23.55, -46.63, 50, 10))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("raio");

        verify(paradaRepository, never())
                .buscarNaCaixa(anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    @DisplayName("Ordena as paradas próximas da mais perto para a mais longe e descarta as fora do raio")
    void ordenaParadasProximas() {
        Parada perto = paradaDe("A", "Perto", -23.5505, -46.6333);        // na coordenada
        Parada media = paradaDe("B", "Media", -23.5560, -46.6333);        // ~0,6 km
        Parada longe = paradaDe("C", "Longe", -23.5900, -46.6333);        // ~4,4 km

        when(paradaRepository.buscarNaCaixa(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(longe, perto, media));

        List<ParadaProximaResponse> proximas =
                paradaService.buscarProximas(-23.5505, -46.6333, 1.0, 10);

        assertThat(proximas).hasSize(2);
        assertThat(proximas.get(0).parada().nome()).isEqualTo("Perto");
        assertThat(proximas.get(1).parada().nome()).isEqualTo("Media");
        assertThat(proximas.get(0).distanciaKm())
                .isLessThan(proximas.get(1).distanciaKm());
    }

    @Test
    @DisplayName("Informa o tempo estimado a pe junto com a distância")
    void calculaTempoCaminhando() {
        Parada parada = paradaDe("A", "Perto", -23.5505, -46.6333);
        when(paradaRepository.buscarNaCaixa(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(parada));

        List<ParadaProximaResponse> proximas =
                paradaService.buscarProximas(-23.5560, -46.6333, 1.0, 10);

        // cerca de 0,6 km a 5 km/h equivale a aproximadamente 8 minutos de caminhada
        assertThat(proximas.get(0).minutosCaminhando()).isBetween(5, 10);
    }

    @Test
    @DisplayName("Respeita o limite de resultados pedido pelo aplicativo")
    void respeitaLimiteDeResultados() {
        when(paradaRepository.buscarNaCaixa(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenReturn(List.of(
                        paradaDe("A", "Uma", -23.5505, -46.6333),
                        paradaDe("B", "Duas", -23.5510, -46.6333),
                        paradaDe("C", "Tres", -23.5515, -46.6333)));

        assertThat(paradaService.buscarProximas(-23.5505, -46.6333, 1.0, 2)).hasSize(2);
    }
}
