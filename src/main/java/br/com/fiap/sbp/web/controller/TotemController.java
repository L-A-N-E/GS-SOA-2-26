package br.com.fiap.sbp.web.controller;

import br.com.fiap.sbp.domain.enums.StatusTotem;
import br.com.fiap.sbp.exception.ErroResposta;
import br.com.fiap.sbp.service.TotemService;
import br.com.fiap.sbp.web.dto.PainelTotemResponse;
import br.com.fiap.sbp.web.dto.TotemRequest;
import br.com.fiap.sbp.web.dto.TotemResponse;
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
@RequestMapping("/api/totens")
@Tag(name = "Totens", description = "Totens instalados na calçada e o painel que eles exibem")
public class TotemController {

    private final TotemService totemService;

    public TotemController(TotemService totemService) {
        this.totemService = totemService;
    }

    @GetMapping("/{codigo}/painel")
    @Operation(summary = "Painel completo do totem",
            description = """
                    O endpoint central do S.B.P. Entrega, em uma única chamada, tudo o que a tela
                    do totem precisa: a parada, o painel permanente com todas as linhas, o destaque
                    do carrossel (veículo a menos de 3 minutos), as referências urbanas próximas,
                    o endereço do QR Code e o texto que o botão tátil le em voz alta.

                    O totem fica na calçada, muitas vezes com conexão instável. Por isso a API
                    entrega a tela pronta, em vez de obrigar o equipamento a fazer cinco chamadas.
                    Cada consulta também registra o batimento do totem, atualizando a última
                    sincronização.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Painel montado"),
            @ApiResponse(responseCode = "404", description = "Totem não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "409", description = "Totem fora de operação",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public ResponseEntity<PainelTotemResponse> painel(
            @Parameter(description = "Código de patrimônio do totem", example = "SBP-0001")
            @PathVariable String codigo) {
        return ResponseEntity.ok(totemService.montarPainel(codigo));
    }

    @GetMapping("/qrcode/{token}")
    @Operation(summary = "Abre o ponto a partir do QR Code lido pelo cidadão",
            description = "A ponte entre o totem físico e o celular. Quem le o QR Code na parada "
                    + "recebe o mesmo painel no navegador ou no aplicativo, sem precisar "
                    + "procurar o ponto manualmente.")
    public ResponseEntity<PainelTotemResponse> painelPorQrCode(
            @Parameter(description = "Token impresso no QR Code", example = "sbp-a1b2c3d4e5f6")
            @PathVariable String token) {
        return ResponseEntity.ok(totemService.montarPainelPorQrCode(token));
    }

    @GetMapping
    @Operation(summary = "Lista os totens", description = "Permite filtrar pela situação operacional.")
    public ResponseEntity<Page<TotemResponse>> listar(
            @Parameter(description = "Situação do totem", example = "ATIVO")
            @RequestParam(required = false) StatusTotem status,
            @PageableDefault(size = 20, sort = "codigo") Pageable paginacao) {
        return ResponseEntity.ok(totemService.listar(status, paginacao));
    }

    @GetMapping("/{codigo}")
    @Operation(summary = "Busca um totem pelo código")
    public ResponseEntity<TotemResponse> buscarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(totemService.buscarPorCodigo(codigo));
    }

    @GetMapping("/atencao")
    @Operation(summary = "Totens que a operação precisa verificar",
            description = "Totens marcados como ativos que pararam de sincronizar há mais de 15 "
                    + "minutos. Um totem mudo continua ligado na rua mostrando informação velha, "
                    + "e por isso precisa ser detectado.")
    public ResponseEntity<List<TotemResponse>> listarQuePrecisamAtencao() {
        return ResponseEntity.ok(totemService.buscarQuePrecisamAtencao());
    }

    @PostMapping
    @Operation(summary = "Instala um totem em uma parada",
            description = "Gera automaticamente o token do QR Code. Uma parada so pode ter um totem.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Totem instalado"),
            @ApiResponse(responseCode = "409", description = "Código repetido ou parada já atendida",
                    content = @Content(schema = @Schema(implementation = ErroResposta.class)))
    })
    public ResponseEntity<TotemResponse> instalar(@RequestBody @Valid TotemRequest requisicao,
                                                  UriComponentsBuilder construtorDeUri) {
        TotemResponse instalado = totemService.instalar(requisicao);
        return ResponseEntity
                .created(construtorDeUri.path("/api/totens/{codigo}")
                        .buildAndExpand(instalado.codigo()).toUri())
                .body(instalado);
    }

    @PatchMapping("/{codigo}/status")
    @Operation(summary = "Altera a situação operacional do totem")
    public ResponseEntity<TotemResponse> alterarStatus(
            @PathVariable String codigo,
            @Parameter(description = "Nova situação", example = "MANUTENCAO")
            @RequestParam StatusTotem status) {
        return ResponseEntity.ok(totemService.alterarStatus(codigo, status));
    }

    @PostMapping("/{codigo}/sincronizar")
    @Operation(summary = "Batimento periódico enviado pelo totem",
            description = "O próprio equipamento chama este endpoint para avisar que está vivo. "
                    + "Um totem que estava sem comunicação volta a ficar ativo.")
    public ResponseEntity<TotemResponse> sincronizar(@PathVariable String codigo) {
        return ResponseEntity.ok(totemService.sincronizar(codigo));
    }

    @DeleteMapping("/{codigo}")
    @Operation(summary = "Remove um totem")
    @ApiResponse(responseCode = "204", description = "Totem removido")
    public ResponseEntity<Void> remover(@PathVariable String codigo) {
        totemService.remover(codigo);
        return ResponseEntity.noContent().build();
    }
}
