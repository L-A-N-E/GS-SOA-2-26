package br.com.fiap.sbp.service;

import br.com.fiap.sbp.domain.entity.Parada;
import br.com.fiap.sbp.domain.entity.PrevisaoChegada;
import br.com.fiap.sbp.domain.entity.Totem;
import br.com.fiap.sbp.domain.enums.StatusTotem;
import br.com.fiap.sbp.domain.repository.TotemRepository;
import br.com.fiap.sbp.exception.RecursoNaoEncontradoException;
import br.com.fiap.sbp.exception.RegraDeNegocioException;
import br.com.fiap.sbp.web.dto.*;
import br.com.fiap.sbp.web.mapper.SbpMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Regras de negócio dos totens.
 *
 * <p>Concentra o endpoint central da API: o painel do totem. O equipamento fica
 * na calçada, muitas vezes com conexão instável, e por isso recebe a tela
 * inteira pronta em uma única chamada, em vez de montar a informação com várias
 * requisições.</p>
 */
@Service
public class TotemService {

    private final TotemRepository totemRepository;
    private final ParadaService paradaService;
    private final PrevisaoService previsaoService;
    private final PontoInteresseService pontoInteresseService;

    /** Base do endereço aberto pelo QR Code impresso no totem. */
    private final String baseUrlQrCode;

    public TotemService(TotemRepository totemRepository,
                        ParadaService paradaService,
                        PrevisaoService previsaoService,
                        PontoInteresseService pontoInteresseService,
                        @Value("${sbp.qrcode.base-url:https://sbp.mobi/p/}") String baseUrlQrCode) {
        this.totemRepository = totemRepository;
        this.paradaService = paradaService;
        this.previsaoService = previsaoService;
        this.pontoInteresseService = pontoInteresseService;
        this.baseUrlQrCode = baseUrlQrCode;
    }

    @Transactional(readOnly = true)
    public Page<TotemResponse> listar(StatusTotem status, Pageable paginacao) {
        Page<Totem> pagina = (status == null)
                ? totemRepository.findAll(paginacao)
                : totemRepository.findByStatus(status, paginacao);
        return pagina.map(SbpMapper::paraResposta);
    }

    @Transactional(readOnly = true)
    public TotemResponse buscarPorCodigo(String codigo) {
        return SbpMapper.paraResposta(buscarEntidadePorCodigo(codigo));
    }

    /** Totens que a operação precisa verificar: em manutenção ou sem sincronizar. */
    @Transactional(readOnly = true)
    public List<TotemResponse> buscarQuePrecisamAtencao() {
        LocalDateTime limite = LocalDateTime.now()
                .minusMinutes(Totem.MINUTOS_LIMITE_SINCRONIZACAO);
        return totemRepository.buscarSemComunicacao(limite)
                .stream().map(SbpMapper::paraResposta).toList();
    }

    /**
     * Monta o painel completo exibido na tela do totem.
     *
     * <p>Reúne, de uma vez: a parada, o painel permanente com todas as linhas, o
     * destaque do carrossel (veículo a menos de 3 minutos), as referências urbanas
     * próximas, o endereço do QR Code e o texto que o botão tátil lê em voz
     * alta.</p>
     */
    @Transactional
    public PainelTotemResponse montarPainel(String codigoTotem) {
        Totem totem = buscarEntidadePorCodigo(codigoTotem);

        if (!totem.getStatus().operacional()) {
            throw new RegraDeNegocioException(
                    "O totem " + codigoTotem + " não está em operação. Situação atual: "
                            + totem.getStatus().getDescricao() + ".");
        }

        totem.sincronizar();
        totemRepository.save(totem);

        Parada parada = totem.getParada();
        List<PrevisaoChegada> vigentes = previsaoService.buscarEntidadesVigentes(parada.getId());
        List<PrevisaoResponse> previsoes = vigentes.stream()
                .map(SbpMapper::paraResposta).toList();

        PrevisaoResponse destaque = previsaoService
                .buscarDestaqueDoCarrossel(parada.getId())
                .map(SbpMapper::paraResposta)
                .orElse(null);

        List<PontoInteresseResponse> referencias = pontoInteresseService
                .buscarProximasDaParada(parada.getId(),
                        PontoInteresseService.RAIO_PADRAO_KM, 5);

        return new PainelTotemResponse(
                totem.getCodigo(),
                SbpMapper.paraResposta(parada),
                baseUrlQrCode + totem.getTokenQrCode(),
                previsoes,
                destaque,
                referencias,
                montarTextoAcessivel(parada, previsoes),
                LocalDateTime.now()
        );
    }

    /**
     * Abre o ponto a partir do QR Code lido pelo cidadão.
     * É a ponte entre o totem físico e o celular: o mesmo painel, no bolso.
     */
    @Transactional
    public PainelTotemResponse montarPainelPorQrCode(String token) {
        Totem totem = totemRepository.findByTokenQrCode(token)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nenhum totem corresponde a este QR Code: " + token));
        return montarPainel(totem.getCodigo());
    }

    @Transactional
    public TotemResponse instalar(TotemRequest requisicao) {
        if (totemRepository.existsByCodigoIgnoreCase(requisicao.codigo())) {
            throw new RegraDeNegocioException(
                    "Já existe um totem com o código " + requisicao.codigo() + ".");
        }

        Parada parada = paradaService.buscarEntidade(requisicao.paradaId());
        Optional<Totem> jaInstalado = totemRepository.findByParadaId(parada.getId());
        if (jaInstalado.isPresent()) {
            throw new RegraDeNegocioException(
                    "A parada " + parada.getNome() + " já possui o totem "
                            + jaInstalado.get().getCodigo() + " instalado.");
        }

        Totem totem = Totem.builder()
                .codigo(requisicao.codigo())
                .parada(parada)
                .status(requisicao.status() == null ? StatusTotem.ATIVO : requisicao.status())
                .tokenQrCode(gerarTokenQrCode())
                .versaoFirmware(requisicao.versaoFirmware())
                .possuiAudioTts(requisicao.possuiAudioTts())
                .possuiBraille(requisicao.possuiBraille())
                .instaladoEm(LocalDateTime.now())
                .ultimaSincronizacao(LocalDateTime.now())
                .build();

        return SbpMapper.paraResposta(totemRepository.save(totem));
    }

    @Transactional
    public TotemResponse alterarStatus(String codigo, StatusTotem novoStatus) {
        Totem totem = buscarEntidadePorCodigo(codigo);
        totem.setStatus(novoStatus);
        return SbpMapper.paraResposta(totemRepository.save(totem));
    }

    /** Batimento periódico enviado pelo próprio totem para avisar que está em operação. */
    @Transactional
    public TotemResponse sincronizar(String codigo) {
        Totem totem = buscarEntidadePorCodigo(codigo);
        totem.sincronizar();
        return SbpMapper.paraResposta(totemRepository.save(totem));
    }

    @Transactional
    public void remover(String codigo) {
        totemRepository.delete(buscarEntidadePorCodigo(codigo));
    }

    @Transactional(readOnly = true)
    public Totem buscarEntidadePorCodigo(String codigo) {
        return totemRepository.findByCodigoIgnoreCase(codigo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Totem", codigo));
    }

    /**
     * Texto lido em voz alta pelo botão tátil, em conformidade com a Lei
     * Brasileira de Inclusão. Anuncia a parada e as três próximas chegadas.
     */
    private String montarTextoAcessivel(Parada parada, List<PrevisaoResponse> previsoes) {
        if (previsoes.isEmpty()) {
            return parada.descricaoAcessivel()
                    + ". No momento não há previsão de chegada disponível para esta parada.";
        }
        String chegadas = previsoes.stream()
                .limit(3)
                .map(PrevisaoResponse::textoAcessivel)
                .collect(Collectors.joining(" "));
        return parada.descricaoAcessivel() + ". " + chegadas;
    }

    private String gerarTokenQrCode() {
        return "sbp-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
