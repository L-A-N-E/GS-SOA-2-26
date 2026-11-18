package br.com.fiap.sbp.service;

import br.com.fiap.sbp.domain.entity.Cidadao;
import br.com.fiap.sbp.domain.entity.RotinaProgramada;
import br.com.fiap.sbp.domain.enums.DiaSemana;
import br.com.fiap.sbp.domain.repository.CidadaoRepository;
import br.com.fiap.sbp.domain.repository.RotinaProgramadaRepository;
import br.com.fiap.sbp.exception.RecursoNaoEncontradoException;
import br.com.fiap.sbp.exception.RegraDeNegocioException;
import br.com.fiap.sbp.web.dto.*;
import br.com.fiap.sbp.web.mapper.SbpMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Regras de negócio do cidadão e das suas rotinas.
 *
 * <p>A rotina programada é o que torna o aplicativo útil no dia a dia: no
 * horário e no dia certos, a tela inicial já abre com a linha que a pessoa
 * costuma pegar, sem necessidade de pesquisa.</p>
 */
@Service
public class CidadaoService {

    /** Limite de rotinas por cidadão, para manter a tela inicial legível. */
    private static final int MAXIMO_ROTINAS = 10;

    private final CidadaoRepository cidadaoRepository;
    private final RotinaProgramadaRepository rotinaRepository;
    private final LinhaService linhaService;
    private final ParadaService paradaService;

    public CidadaoService(CidadaoRepository cidadaoRepository,
                          RotinaProgramadaRepository rotinaRepository,
                          LinhaService linhaService,
                          ParadaService paradaService) {
        this.cidadaoRepository = cidadaoRepository;
        this.rotinaRepository = rotinaRepository;
        this.linhaService = linhaService;
        this.paradaService = paradaService;
    }

    @Transactional(readOnly = true)
    public Page<CidadaoResponse> listar(Pageable paginacao) {
        return cidadaoRepository.findAll(paginacao).map(SbpMapper::paraResposta);
    }

    @Transactional(readOnly = true)
    public CidadaoResponse buscarPorId(Long id) {
        return SbpMapper.paraResposta(buscarEntidade(id));
    }

    @Transactional
    public CidadaoResponse criar(CidadaoRequest requisicao) {
        if (cidadaoRepository.existsByEmailIgnoreCase(requisicao.email())) {
            throw new RegraDeNegocioException(
                    "Já existe um cadastro com o e-mail " + requisicao.email() + ".");
        }
        return SbpMapper.paraResposta(cidadaoRepository.save(SbpMapper.paraEntidade(requisicao)));
    }

    @Transactional
    public CidadaoResponse atualizar(Long id, CidadaoRequest requisicao) {
        Cidadao cidadao = buscarEntidade(id);
        boolean mudouEmail = !cidadao.getEmail().equalsIgnoreCase(requisicao.email());
        if (mudouEmail && cidadaoRepository.existsByEmailIgnoreCase(requisicao.email())) {
            throw new RegraDeNegocioException(
                    "Já existe outro cadastro com o e-mail " + requisicao.email() + ".");
        }
        cidadao.setNome(requisicao.nome());
        cidadao.setEmail(requisicao.email());
        cidadao.setPrefereAltoContraste(requisicao.prefereAltoContraste());
        cidadao.setPrefereLeitorTela(requisicao.prefereLeitorTela());
        cidadao.setPrefereAlertaVibratorio(requisicao.prefereAlertaVibratorio());
        return SbpMapper.paraResposta(cidadaoRepository.save(cidadao));
    }

    @Transactional
    public void remover(Long id) {
        cidadaoRepository.delete(buscarEntidade(id));
    }

    // ------------------------------------------------------------------ Rotinas

    @Transactional(readOnly = true)
    public List<RotinaResponse> listarRotinas(Long cidadaoId) {
        buscarEntidade(cidadaoId);
        return rotinaRepository.findByCidadaoId(cidadaoId)
                .stream().map(SbpMapper::paraResposta).toList();
    }

    /**
     * Card Rota de Hoje: as rotinas ativas do cidadão para o dia de hoje,
     * começando pelas que estão dentro da janela do horário habitual.
     */
    @Transactional(readOnly = true)
    public List<RotinaResponse> buscarRotaDeHoje(Long cidadaoId) {
        buscarEntidade(cidadaoId);
        DiaSemana hoje = DiaSemana.de(LocalDate.now().getDayOfWeek());
        LocalTime agora = LocalTime.now();

        return rotinaRepository.buscarAtivasNoDia(cidadaoId, hoje).stream()
                .map(SbpMapper::paraResposta)
                .sorted((primeira, segunda) -> {
                    boolean a = primeira.ativaAgora();
                    boolean b = segunda.ativaAgora();
                    if (a != b) {
                        return a ? -1 : 1;
                    }
                    return primeira.horarioHabitual().compareTo(segunda.horarioHabitual());
                })
                .toList();
    }

    @Transactional
    public RotinaResponse criarRotina(Long cidadaoId, RotinaRequest requisicao) {
        Cidadao cidadao = buscarEntidade(cidadaoId);

        if (rotinaRepository.findByCidadaoId(cidadaoId).size() >= MAXIMO_ROTINAS) {
            throw new RegraDeNegocioException(
                    "O cidadão já possui o máximo de " + MAXIMO_ROTINAS + " rotinas salvas.");
        }
        if (requisicao.paradaEmbarqueId().equals(requisicao.paradaDesembarqueId())) {
            throw new RegraDeNegocioException(
                    "A parada de embarque e a de desembarque não podem ser a mesma.");
        }

        RotinaProgramada rotina = RotinaProgramada.builder()
                .cidadao(cidadao)
                .linha(linhaService.buscarEntidade(requisicao.linhaId()))
                .paradaEmbarque(paradaService.buscarEntidade(requisicao.paradaEmbarqueId()))
                .paradaDesembarque(paradaService.buscarEntidade(requisicao.paradaDesembarqueId()))
                .horarioHabitual(requisicao.horarioHabitual())
                .dias(requisicao.dias())
                .alertaDesembarque(requisicao.alertaDesembarque())
                .ativa(true)
                .build();

        return SbpMapper.paraResposta(rotinaRepository.save(rotina));
    }

    @Transactional
    public void removerRotina(Long cidadaoId, Long rotinaId) {
        RotinaProgramada rotina = rotinaRepository.findById(rotinaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Rotina", rotinaId));
        if (!rotina.getCidadao().getId().equals(cidadaoId)) {
            throw new RegraDeNegocioException(
                    "A rotina " + rotinaId + " não pertence ao cidadão " + cidadaoId + ".");
        }
        rotinaRepository.delete(rotina);
    }

    @Transactional(readOnly = true)
    public Cidadao buscarEntidade(Long id) {
        return cidadaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cidadão", id));
    }
}
