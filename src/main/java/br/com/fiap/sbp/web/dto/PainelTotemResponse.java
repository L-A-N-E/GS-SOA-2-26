package br.com.fiap.sbp.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tudo o que a tela do totem precisa exibir, em uma unica chamada.
 *
 * <p>O totem fica na calcada, muitas vezes com conexao instavel. Em vez de
 * obrigar o equipamento a fazer cinco chamadas para montar a tela, a API entrega
 * o painel pronto: parada, previsoes, destaque do carrossel, referencias urbanas
 * e o texto de acessibilidade ja montado.</p>
 */
@Schema(description = "Painel completo exibido na tela do totem")
public record PainelTotemResponse(

        @Schema(description = "Código do totem", example = "SBP-0001")
        String codigoTotem,

        @Schema(description = "Parada onde o totem está instalado")
        ParadaResponse parada,

        @Schema(description = "Endereço que o QR Code do totem abre no celular do cidadão",
                example = "https://sbp.mobi/p/sbp-a1b2c3d4e5f6")
        String urlQrCode,

        @Schema(description = "Painel permanente: todas as linhas da parada, da mais próxima a mais distante")
        List<PrevisaoResponse> previsoes,

        @Schema(description = "Card principal do carrossel: veículo a menos de 3 minutos, quando houver")
        PrevisaoResponse destaqueCarrossel,

        @Schema(description = "Referências urbanas próximas, exibidas no mapa da tela")
        List<PontoInteresseResponse> referenciasProximas,

        @Schema(description = "Texto completo lido em voz alta quando o cidadão aciona o botão tátil",
                example = "Parada Av. Paulista, 1578, Em frente ao MASP. O ônibus da linha 8000-10 chega em 7 minutos.")
        String textoAcessivelCompleto,

        @Schema(description = "Momento em que o painel foi montado", example = "2026-11-19T21:35:00")
        LocalDateTime atualizadoEm
) {
}
