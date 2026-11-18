package br.com.fiap.sbp.service;

import br.com.fiap.sbp.domain.entity.Parada;
import br.com.fiap.sbp.domain.repository.ItinerarioRepository;
import br.com.fiap.sbp.domain.repository.ParadaRepository;
import br.com.fiap.sbp.exception.RecursoNaoEncontradoException;
import br.com.fiap.sbp.exception.RegraDeNegocioException;
import br.com.fiap.sbp.web.dto.*;
import br.com.fiap.sbp.web.mapper.SbpMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Regras de negócio das paradas de ônibus.
 *
 * <p>Além do cadastro, concentra a busca por proximidade, que é a porta de
 * entrada do aplicativo e do site: qual é o ponto mais perto de mim.</p>
 */
@Service
public class ParadaService {

    /** Grau de latitude em quilômetros, usado para montar a caixa de busca. */
    private static final double KM_POR_GRAU_LATITUDE = 111.0;

    /** Raio máximo aceito na busca por proximidade, para não varrer a cidade inteira. */
    private static final double RAIO_MAXIMO_KM = 5.0;

    private final ParadaRepository paradaRepository;
    private final ItinerarioRepository itinerarioRepository;

    public ParadaService(ParadaRepository paradaRepository,
                         ItinerarioRepository itinerarioRepository) {
        this.paradaRepository = paradaRepository;
        this.itinerarioRepository = itinerarioRepository;
    }

    @Transactional(readOnly = true)
    public Page<ParadaResponse> listar(String nome, Pageable paginacao) {
        Page<Parada> paginaDeParadas = (nome == null || nome.isBlank())
                ? paradaRepository.findAll(paginacao)
                : paradaRepository.findByNomeContainingIgnoreCase(nome.trim(), paginacao);
        return paginaDeParadas.map(SbpMapper::paraResposta);
    }

    @Transactional(readOnly = true)
    public ParadaResponse buscarPorId(Long id) {
        return SbpMapper.paraResposta(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public ParadaResponse buscarPorCodigoGtfs(String codigoGtfs) {
        Parada parada = paradaRepository.findByCodigoGtfs(codigoGtfs)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Parada", codigoGtfs));
        return SbpMapper.paraResposta(parada);
    }

    /**
     * Paradas dentro de um raio, da mais perto para a mais longe.
     *
     * <p>O banco filtra primeiro por uma caixa de coordenadas, aproveitando o
     * índice; a distância exata é calculada depois, em memória, pela fórmula de
     * Haversine. O pré-filtro evita calcular a distância das 22 mil paradas da
     * cidade a cada consulta.</p>
     */
    @Transactional(readOnly = true)
    public List<ParadaProximaResponse> buscarProximas(double latitude, double longitude,
                                                      double raioKm, int limite) {
        if (raioKm <= 0 || raioKm > RAIO_MAXIMO_KM) {
            throw new RegraDeNegocioException(
                    "O raio de busca deve estar entre 0 e " + RAIO_MAXIMO_KM + " km.");
        }

        double margemLatitude = raioKm / KM_POR_GRAU_LATITUDE;
        double margemLongitude = raioKm
                / (KM_POR_GRAU_LATITUDE * Math.cos(Math.toRadians(latitude)));

        return paradaRepository.buscarNaCaixa(
                        latitude - margemLatitude, latitude + margemLatitude,
                        longitude - margemLongitude, longitude + margemLongitude)
                .stream()
                .map(parada -> SbpMapper.paraRespostaProxima(parada,
                        parada.distanciaKm(latitude, longitude)))
                .filter(resposta -> resposta.distanciaKm() <= raioKm)
                .sorted(Comparator.comparingDouble(ParadaProximaResponse::distanciaKm))
                .limit(limite)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ItinerarioResponse> buscarTrajetoDaLinha(Long linhaId) {
        return itinerarioRepository.buscarTrajetoDaLinha(linhaId)
                .stream().map(SbpMapper::paraResposta).toList();
    }

    @Transactional
    public ParadaResponse criar(ParadaRequest requisicao) {
        if (paradaRepository.existsByCodigoGtfs(requisicao.codigoGtfs())) {
            throw new RegraDeNegocioException(
                    "Já existe uma parada com o código GTFS " + requisicao.codigoGtfs() + ".");
        }
        Parada parada = paradaRepository.save(SbpMapper.paraEntidade(requisicao));
        return SbpMapper.paraResposta(parada);
    }

    @Transactional
    public ParadaResponse atualizar(Long id, ParadaRequest requisicao) {
        Parada parada = buscarEntidade(id);
        boolean mudouCodigo = !parada.getCodigoGtfs().equals(requisicao.codigoGtfs());
        if (mudouCodigo && paradaRepository.existsByCodigoGtfs(requisicao.codigoGtfs())) {
            throw new RegraDeNegocioException(
                    "Já existe outra parada com o código GTFS " + requisicao.codigoGtfs() + ".");
        }
        SbpMapper.copiarPara(requisicao, parada);
        return SbpMapper.paraResposta(paradaRepository.save(parada));
    }

    @Transactional
    public void remover(Long id) {
        Parada parada = buscarEntidade(id);
        if (parada.getTotem() != null) {
            throw new RegraDeNegocioException(
                    "A parada não pode ser removida porque ainda existe um totem instalado nela. "
                            + "Remova o totem " + parada.getTotem().getCodigo() + " antes.");
        }
        paradaRepository.delete(parada);
    }

    /** Acesso interno à entidade, usado pelos demais serviços. */
    @Transactional(readOnly = true)
    public Parada buscarEntidade(Long id) {
        return paradaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Parada", id));
    }
}
