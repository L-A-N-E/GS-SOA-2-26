package br.com.fiap.sbp.web.controller;

import br.com.fiap.sbp.domain.enums.CategoriaPontoInteresse;
import br.com.fiap.sbp.service.PontoInteresseService;
import br.com.fiap.sbp.web.dto.PontoInteresseRequest;
import br.com.fiap.sbp.web.dto.PontoInteresseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pontos-interesse")
@Validated
@Tag(name = "Pontos de interesse",
        description = "Referências urbanas exibidas no mapa do totem")
public class PontoInteresseController {

    private final PontoInteresseService pontoInteresseService;

    public PontoInteresseController(PontoInteresseService pontoInteresseService) {
        this.pontoInteresseService = pontoInteresseService;
    }

    @GetMapping
    @Operation(summary = "Lista e busca referências urbanas",
            description = "Permite filtrar por parte do nome ou por categoria.")
    public ResponseEntity<Page<PontoInteresseResponse>> listar(
            @Parameter(description = "Parte do nome", example = "Hospital")
            @RequestParam(required = false) String nome,
            @Parameter(description = "Categoria", example = "ESTACAO_METRO")
            @RequestParam(required = false) CategoriaPontoInteresse categoria,
            @PageableDefault(size = 20, sort = "nome") Pageable paginacao) {
        return ResponseEntity.ok(pontoInteresseService.listar(nome, categoria, paginacao));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma referência urbana pelo identificador")
    public ResponseEntity<PontoInteresseResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pontoInteresseService.buscarPorId(id));
    }

    @GetMapping("/parada/{paradaId}")
    @Operation(summary = "Referências próximas a uma parada",
            description = "O que o totem exibe no mapa para que o cidadão se localize por marcos "
                    + "da rua (hospitais, shoppings, estações, teatros) e não apenas pelo nome "
                    + "das paradas. Retorna da mais perto para a mais longe.")
    public ResponseEntity<List<PontoInteresseResponse>> buscarProximasDaParada(
            @PathVariable Long paradaId,
            @Parameter(description = "Raio em quilômetros", example = "1.0")
            @RequestParam(defaultValue = "1.0") double raioKm,
            @Parameter(description = "Quantidade máxima", example = "5")
            @RequestParam(defaultValue = "5") @Min(1) @Max(30) int limite) {
        return ResponseEntity.ok(
                pontoInteresseService.buscarProximasDaParada(paradaId, raioKm, limite));
    }

    @GetMapping("/proximas")
    @Operation(summary = "Referências próximas de uma coordenada")
    public ResponseEntity<List<PontoInteresseResponse>> buscarProximas(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "1.0") double raioKm,
            @RequestParam(defaultValue = "5") @Min(1) @Max(30) int limite) {
        return ResponseEntity.ok(
                pontoInteresseService.buscarProximas(latitude, longitude, raioKm, limite));
    }

    @PostMapping
    @Operation(summary = "Cadastra uma referência urbana")
    @ApiResponse(responseCode = "201", description = "Referência cadastrada")
    public ResponseEntity<PontoInteresseResponse> criar(
            @RequestBody @Valid PontoInteresseRequest requisicao) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pontoInteresseService.criar(requisicao));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma referência urbana")
    @ApiResponse(responseCode = "204", description = "Referência removida")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        pontoInteresseService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
