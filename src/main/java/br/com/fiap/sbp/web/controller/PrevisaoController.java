package br.com.fiap.sbp.web.controller;

import br.com.fiap.sbp.exception.ErroResposta;
import br.com.fiap.sbp.service.PrevisaoService;
import br.com.fiap.sbp.web.dto.PrevisaoRequest;
import br.com.fiap.sbp.web.dto.PrevisaoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/previsoes")
@Tag(name = "Previsões", description = "Previsão de chegada dos ônibus a uma parada")
public class PrevisaoController {

    private final PrevisaoService previsaoService;

    public PrevisaoController(PrevisaoService previsaoService) {
        this.previsaoService = previsaoService;
    }

    @GetMapping("/parada/{paradaId}")
    @Operation(summary = "Previsões vigentes de uma parada",
            description = """
                    Retorna as previsões ainda válidas, da mais próxima para a mais distante.

                    Previsões com mais de 5 minutos são descartadas: um número desatualizado e
                    pior do que nenhum número, porque faz o passageiro perder o ônibus achando
                    que ainda tem tempo.

                    Cada previsão informa a origem do dado, para que o cidadão saiba se está
                    vendo a posição real do veículo, o quadro de horários publicado pela SPTrans
                    ou uma estimativa do modelo de Machine Learning do projeto.
                    """)
    public ResponseEntity<List<PrevisaoResponse>> buscarPorParada(@PathVariable Long paradaId) {
        return ResponseEntity.ok(previsaoService.buscarVigentesPorParada(paradaId));
    }

    @GetMapping("/parada/{paradaId}/linha/{linhaId}")
    @Operation(summary = "Previsões de uma linha específica em uma parada",
            description = "Usado quando o cidadão toca em uma linha na tela do totem para fixar "
                    + "a rota detalhada.")
    public ResponseEntity<List<PrevisaoResponse>> buscarPorParadaELinha(@PathVariable Long paradaId,
                                                                        @PathVariable Long linhaId) {
        return ResponseEntity.ok(previsaoService.buscarVigentesPorParadaELinha(paradaId, linhaId));
    }

    @PostMapping
    @Operation(summary = "Registra uma previsão de chegada",
            description = "Endpoint consumido pela integração com a API Olho Vivo da SPTrans e "
                    + "pelo serviço que aplica o modelo de Machine Learning do projeto.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Previsão registrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "404", description = "Linha ou parada não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "Previsão longa demais para ser útil",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public ResponseEntity<PrevisaoResponse> registrar(@RequestBody @Valid PrevisaoRequest requisicao) {
        return ResponseEntity.status(HttpStatus.CREATED).body(previsaoService.registrar(requisicao));
    }

    @DeleteMapping("/expiradas")
    @Operation(summary = "Remove as previsões expiradas",
            description = "Rotina de manutenção. Sem ela a tabela cresce indefinidamente e o "
                    + "painel fica lento justamente no horário de pico.")
    public ResponseEntity<Map<String, Integer>> limparExpiradas() {
        return ResponseEntity.ok(Map.of("previsoesRemovidas", previsaoService.limparExpiradas()));
    }
}
