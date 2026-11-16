package br.com.fiap.sbp.web.controller;

import br.com.fiap.sbp.exception.ErroResposta;
import br.com.fiap.sbp.service.CidadaoService;
import br.com.fiap.sbp.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/cidadaos")
@Tag(name = "Cidadãos e rotinas",
        description = "Cadastro do passageiro no aplicativo e os trajetos que ele salva")
public class CidadaoController {

    private final CidadaoService cidadaoService;

    public CidadaoController(CidadaoService cidadaoService) {
        this.cidadaoService = cidadaoService;
    }

    @GetMapping
    @Operation(summary = "Lista os cidadãos cadastrados")
    public ResponseEntity<Page<CidadaoResponse>> listar(
            @PageableDefault(size = 20, sort = "nome") Pageable paginacao) {
        return ResponseEntity.ok(cidadaoService.listar(paginacao));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um cidadão pelo identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cidadão encontrado"),
            @ApiResponse(responseCode = "404", description = "Cidadão não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public ResponseEntity<CidadaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cidadaoService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cadastra um cidadão",
            description = "Guarda apenas o mínimo necessário e as preferências de acessibilidade.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cidadão cadastrado"),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public ResponseEntity<CidadaoResponse> criar(@RequestBody @Valid CidadaoRequest requisicao,
                                                 UriComponentsBuilder construtorDeUri) {
        CidadaoResponse criado = cidadaoService.criar(requisicao);
        return ResponseEntity
                .created(construtorDeUri.path("/api/cidadaos/{id}")
                        .buildAndExpand(criado.id()).toUri())
                .body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza o cadastro e as preferências de acessibilidade")
    public ResponseEntity<CidadaoResponse> atualizar(@PathVariable Long id,
                                                     @RequestBody @Valid CidadaoRequest requisicao) {
        return ResponseEntity.ok(cidadaoService.atualizar(id, requisicao));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um cadastro")
    @ApiResponse(responseCode = "204", description = "Cadastro removido")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        cidadaoService.remover(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------------ Rotinas

    @GetMapping("/{id}/rotinas")
    @Operation(summary = "Lista as rotinas salvas pelo cidadão")
    public ResponseEntity<List<RotinaResponse>> listarRotinas(@PathVariable Long id) {
        return ResponseEntity.ok(cidadaoService.listarRotinas(id));
    }

    @GetMapping("/{id}/rota-de-hoje")
    @Operation(summary = "Card Rota de Hoje da tela inicial",
            description = "Retorna as rotinas ativas para o dia de hoje, começando pelas que "
                    + "estão dentro da janela de 30 minutos do horário habitual. E o que faz o "
                    + "aplicativo abrir já mostrando a linha que a pessoa costuma pegar.")
    public ResponseEntity<List<RotinaResponse>> rotaDeHoje(@PathVariable Long id) {
        return ResponseEntity.ok(cidadaoService.buscarRotaDeHoje(id));
    }

    @PostMapping("/{id}/rotinas")
    @Operation(summary = "Salva um trajeto habitual",
            description = "Máximo de 10 rotinas por cidadão. As paradas de embarque e "
                    + "desembarque precisam ser diferentes.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Rotina salva"),
            @ApiResponse(responseCode = "409", description = "Limite atingido ou paradas iguais",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public ResponseEntity<RotinaResponse> criarRotina(@PathVariable Long id,
                                                      @RequestBody @Valid RotinaRequest requisicao) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cidadaoService.criarRotina(id, requisicao));
    }

    @DeleteMapping("/{id}/rotinas/{rotinaId}")
    @Operation(summary = "Remove uma rotina")
    @ApiResponse(responseCode = "204", description = "Rotina removida")
    public ResponseEntity<Void> removerRotina(@PathVariable Long id, @PathVariable Long rotinaId) {
        cidadaoService.removerRotina(id, rotinaId);
        return ResponseEntity.noContent().build();
    }
}
