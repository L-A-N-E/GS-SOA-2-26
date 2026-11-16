package br.com.fiap.sbp.service;

import br.com.fiap.sbp.domain.entity.Linha;
import br.com.fiap.sbp.domain.entity.Parada;
import br.com.fiap.sbp.domain.entity.PrevisaoChegada;
import br.com.fiap.sbp.domain.enums.NivelLotacao;
import br.com.fiap.sbp.domain.repository.PrevisaoChegadaRepository;
import br.com.fiap.sbp.exception.RegraDeNegocioException;
import br.com.fiap.sbp.web.dto.PrevisaoRequest;
import br.com.fiap.sbp.web.dto.PrevisaoResponse;
import br.com.fiap.sbp.web.mapper.SbpMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Regras de negocio das previsoes de chegada.
 *
 * <p>Concentra a decisao de o que pode ser exibido ao cidadao. Uma previsao
 * velha e pior do que nenhuma previsao: o passageiro que ve um numero
 * desatualizado perde o onibus achando que ainda tem tempo. Por isso tudo o que
 * sai daqui passa pelo filtro de validade.</p>
 */
@Service
public class PrevisaoService {

    /** Limite superior aceito para uma previsao, em minutos. */
    private static final int MINUTOS_MAXIMOS = 180;

    private final PrevisaoChegadaRepository previsaoRepository;
    private final LinhaService linhaService;
    private final ParadaService paradaService;

    public PrevisaoService(PrevisaoChegadaRepository previsaoRepository,
                           LinhaService linhaService,
                           ParadaService paradaService) {
        this.previsaoRepository = previsaoRepository;
        this.linhaService = linhaService;
        this.paradaService = paradaService;
    }

    /** Previsoes vigentes de uma parada, da mais proxima para a mais distante. */
    @Transactional(readOnly = true)
    public List<PrevisaoResponse> buscarVigentesPorParada(Long paradaId) {
        paradaService.buscarEntidade(paradaId);
        return buscarEntidadesVigentes(paradaId).stream()
                .map(SbpMapper::paraResposta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PrevisaoResponse> buscarVigentesPorParadaELinha(Long paradaId, Long linhaId) {
        paradaService.buscarEntidade(paradaId);
        linhaService.buscarEntidade(linhaId);
        return previsaoRepository
                .buscarVigentesPorParadaELinha(paradaId, linhaId, inicioDaValidade())
                .stream().map(SbpMapper::paraResposta).toList();
    }

    /**
     * Veiculo a menos de 3 minutos, que ocupa o card principal do carrossel do
     * totem. Entre varios candidatos, vence o mais proximo.
     */
    @Transactional(readOnly = true)
    public Optional<PrevisaoChegada> buscarDestaqueDoCarrossel(Long paradaId) {
        return buscarEntidadesVigentes(paradaId).stream()
                .filter(PrevisaoChegada::emergencia)
                .min(Comparator.comparing(PrevisaoChegada::getMinutosEstimados));
    }

    @Transactional
    public PrevisaoResponse registrar(PrevisaoRequest requisicao) {
        if (requisicao.minutosEstimados() > MINUTOS_MAXIMOS) {
            throw new RegraDeNegocioException(
                    "Uma previsão de mais de " + MINUTOS_MAXIMOS
                            + " minutos não é útil para o cidadão no ponto.");
        }
        validarFaixa(requisicao);

        Linha linha = linhaService.buscarEntidade(requisicao.linhaId());
        Parada parada = paradaService.buscarEntidade(requisicao.paradaId());

        PrevisaoChegada previsao = PrevisaoChegada.builder()
                .linha(linha)
                .parada(parada)
                .minutosEstimados(requisicao.minutosEstimados())
                .origem(requisicao.origem())
                .lotacao(requisicao.lotacao() == null
                        ? NivelLotacao.DESCONHECIDO : requisicao.lotacao())
                .prefixoVeiculo(requisicao.prefixoVeiculo())
                .faixaMinMinutos(requisicao.faixaMinMinutos())
                .faixaMaxMinutos(requisicao.faixaMaxMinutos())
                .geradaEm(LocalDateTime.now())
                .build();

        return SbpMapper.paraResposta(previsaoRepository.save(previsao));
    }

    /**
     * Confere a coerencia da faixa de espera.
     *
     * <p>Uma faixa pela metade, invertida ou que nao contem a propria estimativa
     * central seria exibida ao cidadao como se fosse confiavel. E melhor recusar
     * a previsao do que publicar um intervalo sem sentido no totem.</p>
     */
    private void validarFaixa(PrevisaoRequest requisicao) {
        Integer minimo = requisicao.faixaMinMinutos();
        Integer maximo = requisicao.faixaMaxMinutos();

        if (minimo == null && maximo == null) {
            return;
        }
        if (minimo == null || maximo == null) {
            throw new RegraDeNegocioException(
                    "A faixa de espera precisa dos dois limites: informe o mínimo e o máximo.");
        }
        if (minimo > maximo) {
            throw new RegraDeNegocioException(
                    "O limite inferior da faixa (" + minimo + " min) não pode ser maior "
                            + "que o superior (" + maximo + " min).");
        }
        int estimado = requisicao.minutosEstimados();
        if (estimado < minimo || estimado > maximo) {
            throw new RegraDeNegocioException(
                    "A estimativa de " + estimado + " minutos está fora da própria faixa ("
                            + minimo + " a " + maximo + " min).");
        }
    }

    /**
     * Remove as previsoes que ja nao podem ser exibidas.
     *
     * <p>Sem essa limpeza, a tabela cresce indefinidamente e as consultas do
     * painel ficam lentas justamente nos horarios de pico, quando mais gente
     * esta no ponto.</p>
     */
    @Transactional
    public int limparExpiradas() {
        return previsaoRepository.removerAnterioresA(inicioDaValidade());
    }

    @Transactional(readOnly = true)
    public List<PrevisaoChegada> buscarEntidadesVigentes(Long paradaId) {
        return previsaoRepository.buscarVigentesPorParada(paradaId, inicioDaValidade());
    }

    private LocalDateTime inicioDaValidade() {
        return LocalDateTime.now().minusMinutes(PrevisaoChegada.MINUTOS_VALIDADE);
    }
}
