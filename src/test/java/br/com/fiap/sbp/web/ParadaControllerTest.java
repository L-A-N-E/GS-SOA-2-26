package br.com.fiap.sbp.web;

import br.com.fiap.sbp.exception.RecursoNaoEncontradoException;
import br.com.fiap.sbp.exception.RegraDeNegocioException;
import br.com.fiap.sbp.service.ParadaService;
import br.com.fiap.sbp.web.controller.ParadaController;
import br.com.fiap.sbp.web.dto.ParadaProximaResponse;
import br.com.fiap.sbp.web.dto.ParadaRequest;
import br.com.fiap.sbp.web.dto.ParadaResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testa a camada HTTP das paradas: codigos de resposta, validacao de entrada e
 * o formato padronizado de erro que o aplicativo, o site e o totem consomem.
 */
@WebMvcTest(ParadaController.class)
class ParadaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ParadaService paradaService;

    private ParadaResponse paradaExemplo() {
        return new ParadaResponse(1L, "340015890", "Av. Paulista, 1578", "Em frente ao MASP",
                -23.561414, -46.655881, true, true, true,
                "Hub de integração", "2 - alcance",
                "Parada Av. Paulista, 1578, Em frente ao MASP");
    }

    @Test
    @DisplayName("GET de uma parada existente responde 200 com os dados da parada")
    void buscarParadaExistente() throws Exception {
        when(paradaService.buscarPorId(1L)).thenReturn(paradaExemplo());

        mockMvc.perform(get("/api/paradas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoGtfs").value("340015890"))
                .andExpect(jsonPath("$.nome").value("Av. Paulista, 1578"))
                .andExpect(jsonPath("$.possuiTotemAtivo").value(true))
                .andExpect(jsonPath("$.prioridadeTotem").value("2 - alcance"));
    }

    @Test
    @DisplayName("GET de uma parada inexistente responde 404 no formato padronizado de erro")
    void buscarParadaInexistente() throws Exception {
        when(paradaService.buscarPorId(99L))
                .thenThrow(new RecursoNaoEncontradoException("Parada", 99L));

        mockMvc.perform(get("/api/paradas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.caminho").value("/api/paradas/99"))
                .andExpect(jsonPath("$.mensagem").value(
                        org.hamcrest.Matchers.containsString("99")));
    }

    @Test
    @DisplayName("POST com dados válidos responde 201 e o cabeçalho Location")
    void criarParada() throws Exception {
        ParadaRequest requisicao = new ParadaRequest("340015890", "Av. Paulista, 1578",
                "Em frente ao MASP", -23.561414, -46.655881, true, true);
        when(paradaService.criar(any())).thenReturn(paradaExemplo());

        mockMvc.perform(post("/api/paradas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicao)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.endsWith("/api/paradas/1")))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST sem os campos obrigatórios responde 400 apontando cada campo inválido")
    void criarParadaInvalida() throws Exception {
        String corpoIncompleto = """
                {"codigoGtfs": "", "nome": "", "latitude": null, "longitude": null}
                """;

        mockMvc.perform(post("/api/paradas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoIncompleto))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.camposInvalidos.codigoGtfs").exists())
                .andExpect(jsonPath("$.camposInvalidos.nome").exists())
                .andExpect(jsonPath("$.camposInvalidos.latitude").exists())
                .andExpect(jsonPath("$.camposInvalidos.longitude").exists());
    }

    @Test
    @DisplayName("POST com latitude fora da faixa valida responde 400")
    void criarParadaComLatitudeInvalida() throws Exception {
        ParadaRequest requisicao = new ParadaRequest("340015890", "Parada",
                null, 120.0, -46.655881, false, false);

        mockMvc.perform(post("/api/paradas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requisicao)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.camposInvalidos.latitude").value("Latitude inválida"));
    }

    @Test
    @DisplayName("GET das paradas próximas responde 200 com a distância e o tempo a pe")
    void buscarParadasProximas() throws Exception {
        when(paradaService.buscarProximas(anyDouble(), anyDouble(), anyDouble(), anyInt()))
                .thenReturn(List.of(new ParadaProximaResponse(paradaExemplo(), 0.184, 3)));

        mockMvc.perform(get("/api/paradas/proximas")
                        .param("latitude", "-23.561414")
                        .param("longitude", "-46.655881")
                        .param("raioKm", "0.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].distanciaKm").value(0.184))
                .andExpect(jsonPath("$[0].minutosCaminhando").value(3))
                .andExpect(jsonPath("$[0].parada.nome").value("Av. Paulista, 1578"));
    }

    @Test
    @DisplayName("GET das paradas próximas com raio fora do limite responde 409")
    void buscarParadasProximasComRaioInvalido() throws Exception {
        when(paradaService.buscarProximas(anyDouble(), anyDouble(), anyDouble(), anyInt()))
                .thenThrow(new RegraDeNegocioException("O raio de busca deve estar entre 0 e 5.0 km."));

        mockMvc.perform(get("/api/paradas/proximas")
                        .param("latitude", "-23.561414")
                        .param("longitude", "-46.655881")
                        .param("raioKm", "50"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.erro").value("Conflito com uma regra de negócio"));
    }

    @Test
    @DisplayName("DELETE de uma parada existente responde 204 sem corpo")
    void removerParada() throws Exception {
        mockMvc.perform(delete("/api/paradas/1"))
                .andExpect(status().isNoContent());
    }
}
