package br.com.fiap.sbp.web.controller;

import br.com.fiap.sbp.exception.ErroResposta;
import br.com.fiap.sbp.service.ParadaService;
import br.com.fiap.sbp.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/paradas")
@Validated
@Tag(name = "Paradas", description = "Pontos de parada de ônibus da cidade")
public class ParadaController {

    private final ParadaService paradaService;

    public ParadaController(ParadaService paradaService) {
        this.paradaService = paradaService;
    }

    @GetMapping
    @Operation(summary = "Lista as paradas",
            description = "Retorna as paradas paginadas. Informe o parâmetro nome para filtrar "
                    + "pelo nome da parada, como faz a busca da aba Explorar do aplicativo.")
    public ResponseEntity<Page<ParadaResponse>> listar(
            @Parameter(description = "Filtro por parte do nome da parada", example = "Paulista")
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 20, sort = "nome") Pageable paginacao) {
        return ResponseEntity.ok(paradaService.listar(nome, paginacao));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma parada pelo identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Parada encontrada"),
            @ApiResponse(responseCode = "404", description = "Parada não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public ResponseEntity<ParadaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(paradaService.buscarPorId(id));
    }

    @GetMapping("/codigo-gtfs/{codigoGtfs}")
    @Operation(summary = "Busca uma parada pelo código do GTFS da SPTrans",
            description = "Útil para integrar com o dado aberto da cidade sem depender "
                    + "do identificador interno do S.B.P.")
    public ResponseEntity<ParadaResponse> buscarPorCodigoGtfs(@PathVariable String codigoGtfs) {
        return ResponseEntity.ok(paradaService.buscarPorCodigoGtfs(codigoGtfs));
    }

    @GetMapping("/proximas")
    @Operation(summary = "Busca as paradas mais próximas de uma coordenada",
            description = "Responde a pergunta qual e o ponto mais perto de mim. "
                    + "Retorna as paradas ordenadas pela distância, já com a estimativa "
                    + "de tempo a pé. O raio máximo aceito é de 5 km.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paradas encontradas"),
            @ApiResponse(responseCode = "409", description = "Raio fora do limite permitido",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public ResponseEntity<List<ParadaProximaResponse>> buscarProximas(
            @Parameter(description = "Latitude do cidadão", example = "-23.561414", required = true)
            @RequestParam double latitude,
            @Parameter(description = "Longitude do cidadão", example = "-46.655881", required = true)
            @RequestParam double longitude,
            @Parameter(description = "Raio da busca em quilômetros", example = "0.8")
            @RequestParam(defaultValue = "1.0") double raioKm,
            @Parameter(description = "Quantidade máxima de paradas", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limite) {
        return ResponseEntity.ok(
                paradaService.buscarProximas(latitude, longitude, raioKm, limite));
    }

    @GetMapping("/linha/{linhaId}")
    @Operation(summary = "Trajeto de uma linha",
            description = "Retorna as paradas na ordem em que o ônibus passa. "
                    + "E o que o totem usa para desenhar o mapa de rota.")
    public ResponseEntity<List<ItinerarioResponse>> buscarTrajeto(@PathVariable Long linhaId) {
        return ResponseEntity.ok(paradaService.buscarTrajetoDaLinha(linhaId));
    }

    @PostMapping
    @Operation(summary = "Cadastra uma parada")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Parada cadastrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "Já existe parada com esse código GTFS",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public ResponseEntity<ParadaResponse> criar(@RequestBody @Valid ParadaRequest requisicao,
                                                UriComponentsBuilder construtorDeUri) {
        ParadaResponse criada = paradaService.criar(requisicao);
        return ResponseEntity
                .created(construtorDeUri.path("/api/paradas/{id}")
                        .buildAndExpand(criada.id()).toUri())
                .body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma parada")
    public ResponseEntity<ParadaResponse> atualizar(@PathVariable Long id,
                                                    @RequestBody @Valid ParadaRequest requisicao) {
        return ResponseEntity.ok(paradaService.atualizar(id, requisicao));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma parada",
            description = "A remoção e bloqueada se ainda houver um totem instalado na parada.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Parada removida"),
            @ApiResponse(responseCode = "409", description = "Parada com totem instalado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        paradaService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
