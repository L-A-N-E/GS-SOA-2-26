package br.com.fiap.sbp.web.mapper;

import br.com.fiap.sbp.domain.entity.*;
import br.com.fiap.sbp.web.dto.*;

import java.time.LocalTime;

/**
 * Conversão entre entidades do domínio e os objetos de transporte da API.
 *
 * <p>Manter a conversão em um ponto único evita que as entidades JPA vazem para
 * fora da aplicação. Assim, uma mudança no mapeamento do banco não quebra o
 * contrato consumido pelo aplicativo, pelo site e pelo totem.</p>
 */
public final class SbpMapper {

    /** Velocidade média de caminhada considerada no cálculo a pé, em km/h. */
    private static final double VELOCIDADE_CAMINHADA_KMH = 5.0;

    private SbpMapper() {
        throw new UnsupportedOperationException("Classe utilitaria, não deve ser instanciada");
    }

    // ------------------------------------------------------------------ Parada

    public static ParadaResponse paraResposta(Parada parada) {
        return new ParadaResponse(
                parada.getId(),
                parada.getCodigoGtfs(),
                parada.getNome(),
                parada.getReferenciaUrbana(),
                parada.getLatitude(),
                parada.getLongitude(),
                parada.isAcessivelCadeirante(),
                parada.isPossuiCobertura(),
                parada.possuiTotemAtivo(),
                parada.getGrupoMl(),
                parada.getPrioridadeTotem(),
                parada.descricaoAcessivel()
        );
    }

    public static ParadaProximaResponse paraRespostaProxima(Parada parada, double distanciaKm) {
        int minutos = (int) Math.ceil(distanciaKm / VELOCIDADE_CAMINHADA_KMH * 60);
        return new ParadaProximaResponse(paraResposta(parada),
                Math.round(distanciaKm * 1000.0) / 1000.0, minutos);
    }

    public static Parada paraEntidade(ParadaRequest requisicao) {
        return Parada.builder()
                .codigoGtfs(requisicao.codigoGtfs())
                .nome(requisicao.nome())
                .referenciaUrbana(requisicao.referenciaUrbana())
                .latitude(requisicao.latitude())
                .longitude(requisicao.longitude())
                .acessivelCadeirante(requisicao.acessivelCadeirante())
                .possuiCobertura(requisicao.possuiCobertura())
                .build();
    }

    public static void copiarPara(ParadaRequest requisicao, Parada parada) {
        parada.setCodigoGtfs(requisicao.codigoGtfs());
        parada.setNome(requisicao.nome());
        parada.setReferenciaUrbana(requisicao.referenciaUrbana());
        parada.setLatitude(requisicao.latitude());
        parada.setLongitude(requisicao.longitude());
        parada.setAcessivelCadeirante(requisicao.acessivelCadeirante());
        parada.setPossuiCobertura(requisicao.possuiCobertura());
    }

    // ------------------------------------------------------------------- Linha

    public static LinhaResponse paraResposta(Linha linha) {
        return new LinhaResponse(
                linha.getId(),
                linha.getCodigo(),
                linha.getNome(),
                linha.getSentido(),
                linha.getAreaOperacao(),
                linha.getExtensaoKm(),
                linha.getDuracaoProgramadaMin(),
                linha.isAcessivelCadeirante(),
                linha.noturna(),
                linha.velocidadeMediaKmh()
        );
    }

    public static Linha paraEntidade(LinhaRequest requisicao) {
        return Linha.builder()
                .codigo(requisicao.codigo())
                .nome(requisicao.nome())
                .sentido(requisicao.sentido())
                .areaOperacao(requisicao.areaOperacao())
                .extensaoKm(requisicao.extensaoKm())
                .duracaoProgramadaMin(requisicao.duracaoProgramadaMin())
                .acessivelCadeirante(requisicao.acessivelCadeirante())
                .build();
    }

    public static void copiarPara(LinhaRequest requisicao, Linha linha) {
        linha.setCodigo(requisicao.codigo());
        linha.setNome(requisicao.nome());
        linha.setSentido(requisicao.sentido());
        linha.setAreaOperacao(requisicao.areaOperacao());
        linha.setExtensaoKm(requisicao.extensaoKm());
        linha.setDuracaoProgramadaMin(requisicao.duracaoProgramadaMin());
        linha.setAcessivelCadeirante(requisicao.acessivelCadeirante());
    }

    // ------------------------------------------------------------------- Totem

    public static TotemResponse paraResposta(Totem totem) {
        return new TotemResponse(
                totem.getId(),
                totem.getCodigo(),
                totem.getStatus(),
                totem.getTokenQrCode(),
                totem.getVersaoFirmware(),
                totem.isPossuiAudioTts(),
                totem.isPossuiBraille(),
                totem.plenamenteAcessivel(),
                totem.getInstaladoEm(),
                totem.getUltimaSincronizacao(),
                totem.minutosDesdeUltimaSincronizacao(),
                totem.precisaAtencao(),
                totem.getParada() != null ? paraResposta(totem.getParada()) : null
        );
    }

    // -------------------------------------------------------------- Previsões

    public static PrevisaoResponse paraResposta(PrevisaoChegada previsao) {
        Linha linha = previsao.getLinha();
        Parada parada = previsao.getParada();
        return new PrevisaoResponse(
                previsao.getId(),
                linha != null ? linha.getCodigo() : null,
                linha != null ? linha.getNome() : null,
                parada != null ? parada.getNome() : null,
                previsao.getMinutosEstimados(),
                previsao.getOrigem(),
                previsao.getOrigem() != null ? previsao.getOrigem().getDescricao() : null,
                previsao.getLotacao(),
                previsao.getPrefixoVeiculo(),
                previsao.getFaixaMinMinutos(),
                previsao.getFaixaMaxMinutos(),
                previsao.faixaFormatada(),
                previsao.onibusNoPonto(),
                previsao.emergencia(),
                previsao.textoAcessivel(),
                previsao.getGeradaEm()
        );
    }

    // -------------------------------------------------------- Ponto de interesse

    public static PontoInteresseResponse paraResposta(PontoDeInteresse ponto, Double distanciaKm) {
        return new PontoInteresseResponse(
                ponto.getId(),
                ponto.getNome(),
                ponto.getCategoria(),
                ponto.getCategoria() != null ? ponto.getCategoria().getDescricao() : null,
                ponto.getLatitude(),
                ponto.getLongitude(),
                ponto.getEndereco(),
                distanciaKm == null ? null : Math.round(distanciaKm * 1000.0) / 1000.0
        );
    }

    public static PontoDeInteresse paraEntidade(PontoInteresseRequest requisicao) {
        return PontoDeInteresse.builder()
                .nome(requisicao.nome())
                .categoria(requisicao.categoria())
                .latitude(requisicao.latitude())
                .longitude(requisicao.longitude())
                .endereco(requisicao.endereco())
                .build();
    }

    // ----------------------------------------------------------------- Cidadão

    public static CidadaoResponse paraResposta(Cidadao cidadao) {
        return new CidadaoResponse(
                cidadao.getId(),
                cidadao.getNome(),
                cidadao.getEmail(),
                cidadao.isPrefereAltoContraste(),
                cidadao.isPrefereLeitorTela(),
                cidadao.isPrefereAlertaVibratorio(),
                cidadao.usaRecursoAssistivo(),
                cidadao.getRotinas() == null ? 0 : cidadao.getRotinas().size(),
                cidadao.getCadastradoEm()
        );
    }

    public static Cidadao paraEntidade(CidadaoRequest requisicao) {
        return Cidadao.builder()
                .nome(requisicao.nome())
                .email(requisicao.email())
                .prefereAltoContraste(requisicao.prefereAltoContraste())
                .prefereLeitorTela(requisicao.prefereLeitorTela())
                .prefereAlertaVibratorio(requisicao.prefereAlertaVibratorio())
                .build();
    }

    // ------------------------------------------------------------------ Rotina

    public static RotinaResponse paraResposta(RotinaProgramada rotina) {
        return new RotinaResponse(
                rotina.getId(),
                rotina.getCidadao() != null ? rotina.getCidadao().getId() : null,
                rotina.getLinha() != null ? rotina.getLinha().getCodigo() : null,
                rotina.getLinha() != null ? rotina.getLinha().getNome() : null,
                rotina.getParadaEmbarque() != null ? rotina.getParadaEmbarque().getNome() : null,
                rotina.getParadaDesembarque() != null ? rotina.getParadaDesembarque().getNome() : null,
                rotina.getHorarioHabitual(),
                rotina.getDias(),
                rotina.isAlertaDesembarque(),
                rotina.isAtiva(),
                rotina.isAtiva() && rotina.proximaDoHorario(LocalTime.now())
        );
    }

    // -------------------------------------------------------------- Itinerário

    public static ItinerarioResponse paraResposta(Itinerario itinerario) {
        return new ItinerarioResponse(
                itinerario.getId(),
                itinerario.getOrdem(),
                itinerario.getMinutosDesdeOrigem(),
                itinerario.origem(),
                paraResposta(itinerario.getParada())
        );
    }
}
