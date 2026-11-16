package br.com.fiap.sbp.web.controller;

import br.com.fiap.sbp.domain.enums.AreaOperacao;
import br.com.fiap.sbp.exception.ErroResposta;
import br.com.fiap.sbp.service.LinhaService;
import br.com.fiap.sbp.web.dto.LinhaRequest;
import br.com.fiap.sbp.web.dto.LinhaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/linhas")
@Tag(name = "Linhas", description = "Linhas de ônibus da rede municipal")
public class LinhaController {

    private final LinhaService linhaService;

    public LinhaController(LinhaService linhaService) {
        this.linhaService = linhaService;
    }

    @GetMapping
    @Operation(summary = "Lista e busca linhas",
            description = "A busca unificada da aba Explorar: o parâmetro termo aceita tanto o "
                    + "número da linha quanto parte do nome do trajeto. Também e possível "
                    + "filtrar por área de operação da SPTrans.")
    public ResponseEntity<Page<LinhaResponse>> listar(
            @Parameter(description = "Número da linha ou parte do nome", example = "8000")
            @RequestParam(required = false) String termo,
            @Parameter(description = "Área de operação", example = "OESTE")
            @RequestParam(required = false) AreaOperacao area,
            @PageableDefault(size = 20, sort = "codigo") Pageable paginacao) {
        return ResponseEntity.ok(linhaService.listar(termo, area, paginacao));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma linha pelo identificador")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Linha encontrada"),
            @ApiResponse(responseCode = "404", description = "Linha não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public ResponseEntity<LinhaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(linhaService.buscarPorId(id));
    }

    @GetMapping("/codigo/{codigo}")
    @Operation(summary = "Busca uma linha pelo código da cidade",
            description = "Exemplo: 8000-10. E a forma como o cidadão conhece a linha.")
    public ResponseEntity<LinhaResponse> buscarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(linhaService.buscarPorCodigo(codigo));
    }

    @GetMapping("/parada/{paradaId}")
    @Operation(summary = "Linhas que atendem uma parada",
            description = "Monta o painel permanente do totem, com todas as linhas daquele ponto.")
    public ResponseEntity<List<LinhaResponse>> buscarPorParada(@PathVariable Long paradaId) {
        return ResponseEntity.ok(linhaService.buscarPorParada(paradaId));
    }

    @PostMapping
    @Operation(summary = "Cadastra uma linha")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Linha cadastrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "Já existe linha com esse código",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public ResponseEntity<LinhaResponse> criar(@RequestBody @Valid LinhaRequest requisicao,
                                               UriComponentsBuilder construtorDeUri) {
        LinhaResponse criada = linhaService.criar(requisicao);
        return ResponseEntity
                .created(construtorDeUri.path("/api/linhas/{id}")
                        .buildAndExpand(criada.id()).toUri())
                .body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma linha")
    public ResponseEntity<LinhaResponse> atualizar(@PathVariable Long id,
                                                   @RequestBody @Valid LinhaRequest requisicao) {
        return ResponseEntity.ok(linhaService.atualizar(id, requisicao));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma linha")
    @ApiResponse(responseCode = "204", description = "Linha removida")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        linhaService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
