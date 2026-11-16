package br.com.fiap.sbp.service;

import br.com.fiap.sbp.domain.entity.Linha;
import br.com.fiap.sbp.domain.enums.AreaOperacao;
import br.com.fiap.sbp.domain.repository.LinhaRepository;
import br.com.fiap.sbp.exception.RecursoNaoEncontradoException;
import br.com.fiap.sbp.exception.RegraDeNegocioException;
import br.com.fiap.sbp.web.dto.LinhaRequest;
import br.com.fiap.sbp.web.dto.LinhaResponse;
import br.com.fiap.sbp.web.mapper.SbpMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Regras de negocio das linhas de onibus. */
@Service
public class LinhaService {

    private final LinhaRepository linhaRepository;

    public LinhaService(LinhaRepository linhaRepository) {
        this.linhaRepository = linhaRepository;
    }

    /**
     * Busca unificada da aba Explorar: aceita o numero da linha ou parte do nome
     * do trajeto, e opcionalmente filtra por area de operacao.
     */
    @Transactional(readOnly = true)
    public Page<LinhaResponse> listar(String termo, AreaOperacao area, Pageable paginacao) {
        Page<Linha> pagina;
        if (termo != null && !termo.isBlank()) {
            pagina = linhaRepository.buscarPorTermo(termo.trim(), paginacao);
        } else if (area != null) {
            pagina = linhaRepository.findByAreaOperacao(area, paginacao);
        } else {
            pagina = linhaRepository.findAll(paginacao);
        }
        return pagina.map(SbpMapper::paraResposta);
    }

    @Transactional(readOnly = true)
    public LinhaResponse buscarPorId(Long id) {
        return SbpMapper.paraResposta(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public LinhaResponse buscarPorCodigo(String codigo) {
        Linha linha = linhaRepository.findByCodigoIgnoreCase(codigo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Linha", codigo));
        return SbpMapper.paraResposta(linha);
    }

    /** Linhas que atendem uma parada: monta o painel permanente do totem. */
    @Transactional(readOnly = true)
    public List<LinhaResponse> buscarPorParada(Long paradaId) {
        return linhaRepository.buscarPorParada(paradaId)
                .stream().map(SbpMapper::paraResposta).toList();
    }

    @Transactional
    public LinhaResponse criar(LinhaRequest requisicao) {
        if (linhaRepository.existsByCodigoIgnoreCase(requisicao.codigo())) {
            throw new RegraDeNegocioException(
                    "Já existe uma linha com o código " + requisicao.codigo() + ".");
        }
        return SbpMapper.paraResposta(linhaRepository.save(SbpMapper.paraEntidade(requisicao)));
    }

    @Transactional
    public LinhaResponse atualizar(Long id, LinhaRequest requisicao) {
        Linha linha = buscarEntidade(id);
        boolean mudouCodigo = !linha.getCodigo().equalsIgnoreCase(requisicao.codigo());
        if (mudouCodigo && linhaRepository.existsByCodigoIgnoreCase(requisicao.codigo())) {
            throw new RegraDeNegocioException(
                    "Já existe outra linha com o código " + requisicao.codigo() + ".");
        }
        SbpMapper.copiarPara(requisicao, linha);
        return SbpMapper.paraResposta(linhaRepository.save(linha));
    }

    @Transactional
    public void remover(Long id) {
        linhaRepository.delete(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Linha buscarEntidade(Long id) {
        return linhaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Linha", id));
    }
}
