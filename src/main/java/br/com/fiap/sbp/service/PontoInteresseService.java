package br.com.fiap.sbp.service;

import br.com.fiap.sbp.domain.entity.Parada;
import br.com.fiap.sbp.domain.entity.PontoDeInteresse;
import br.com.fiap.sbp.domain.enums.CategoriaPontoInteresse;
import br.com.fiap.sbp.domain.repository.PontoDeInteresseRepository;
import br.com.fiap.sbp.exception.RecursoNaoEncontradoException;
import br.com.fiap.sbp.web.dto.PontoInteresseRequest;
import br.com.fiap.sbp.web.dto.PontoInteresseResponse;
import br.com.fiap.sbp.web.mapper.SbpMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/** Regras de negócio das referências urbanas exibidas no mapa do totem. */
@Service
public class PontoInteresseService {

    private static final double KM_POR_GRAU_LATITUDE = 111.0;

    /** Raio considerado caminhável a partir da parada. */
    public static final double RAIO_PADRAO_KM = 1.0;

    private final PontoDeInteresseRepository pontoRepository;
    private final ParadaService paradaService;

    public PontoInteresseService(PontoDeInteresseRepository pontoRepository,
                                 ParadaService paradaService) {
        this.pontoRepository = pontoRepository;
        this.paradaService = paradaService;
    }

    @Transactional(readOnly = true)
    public Page<PontoInteresseResponse> listar(String nome, CategoriaPontoInteresse categoria,
                                               Pageable paginacao) {
        Page<PontoDeInteresse> pagina;
        if (nome != null && !nome.isBlank()) {
            pagina = pontoRepository.findByNomeContainingIgnoreCase(nome.trim(), paginacao);
        } else if (categoria != null) {
            pagina = pontoRepository.findByCategoria(categoria, paginacao);
        } else {
            pagina = pontoRepository.findAll(paginacao);
        }
        return pagina.map(ponto -> SbpMapper.paraResposta(ponto, null));
    }

    @Transactional(readOnly = true)
    public PontoInteresseResponse buscarPorId(Long id) {
        PontoDeInteresse ponto = pontoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ponto de interesse", id));
        return SbpMapper.paraResposta(ponto, null);
    }

    /**
     * Referências urbanas próximas a uma parada, da mais perto para a mais longe.
     * É o que o totem exibe no mapa para que o cidadão se localize por marcos da
     * rua, e não apenas pelo nome das paradas.
     */
    @Transactional(readOnly = true)
    public List<PontoInteresseResponse> buscarProximasDaParada(Long paradaId, double raioKm,
                                                               int limite) {
        Parada parada = paradaService.buscarEntidade(paradaId);
        return buscarProximas(parada.getLatitude(), parada.getLongitude(), raioKm, limite);
    }

    @Transactional(readOnly = true)
    public List<PontoInteresseResponse> buscarProximas(double latitude, double longitude,
                                                       double raioKm, int limite) {
        double margemLatitude = raioKm / KM_POR_GRAU_LATITUDE;
        double margemLongitude = raioKm
                / (KM_POR_GRAU_LATITUDE * Math.cos(Math.toRadians(latitude)));

        return pontoRepository.buscarNaCaixa(
                        latitude - margemLatitude, latitude + margemLatitude,
                        longitude - margemLongitude, longitude + margemLongitude)
                .stream()
                .map(ponto -> SbpMapper.paraResposta(ponto,
                        ponto.distanciaKm(latitude, longitude)))
                .filter(resposta -> resposta.distanciaKm() != null
                        && resposta.distanciaKm() <= raioKm)
                .sorted(Comparator.comparingDouble(PontoInteresseResponse::distanciaKm))
                .limit(limite)
                .toList();
    }

    @Transactional
    public PontoInteresseResponse criar(PontoInteresseRequest requisicao) {
        PontoDeInteresse salvo = pontoRepository.save(SbpMapper.paraEntidade(requisicao));
        return SbpMapper.paraResposta(salvo, null);
    }

    @Transactional
    public void remover(Long id) {
        PontoDeInteresse ponto = pontoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ponto de interesse", id));
        pontoRepository.delete(ponto);
    }
}
