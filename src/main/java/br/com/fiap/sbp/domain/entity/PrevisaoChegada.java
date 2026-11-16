package br.com.fiap.sbp.domain.entity;

import br.com.fiap.sbp.domain.enums.NivelLotacao;
import br.com.fiap.sbp.domain.enums.OrigemPrevisao;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Previsao de chegada de uma linha a uma parada.
 *
 * <p>E o dado central do S.B.P.: a resposta para a pergunta que o cidadao faz na
 * calcada. A origem registra de onde a informacao veio, porque o passageiro tem
 * o direito de saber se esta vendo a posicao real do veiculo, o quadro de
 * horarios publicado ou uma estimativa do modelo de Machine Learning.</p>
 */
@Entity
@Table(name = "tb_previsao_chegada", indexes = {
        @Index(name = "ix_previsao_parada", columnList = "parada_id"),
        @Index(name = "ix_previsao_linha", columnList = "linha_id"),
        @Index(name = "ix_previsao_gerada", columnList = "gerada_em")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrevisaoChegada {

    /** Abaixo desse tempo o veiculo ocupa o card principal do carrossel do totem. */
    public static final int MINUTOS_EMERGENCIA = 3;

    /** Depois desse tempo a previsao e considerada velha demais para ser exibida. */
    public static final int MINUTOS_VALIDADE = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A previsão precisa estar vinculada a uma linha")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "linha_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_previsao_linha"))
    private Linha linha;

    @NotNull(message = "A previsão precisa estar vinculada a uma parada")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parada_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_previsao_parada"))
    private Parada parada;

    @NotNull(message = "O tempo estimado em minutos é obrigatório")
    @PositiveOrZero(message = "O tempo estimado não pode ser negativo")
    @Column(name = "minutos_estimados", nullable = false)
    private Integer minutosEstimados;

    @NotNull(message = "A origem da previsão é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(name = "origem", nullable = false, length = 20)
    private OrigemPrevisao origem;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "lotacao", nullable = false, length = 20)
    private NivelLotacao lotacao = NivelLotacao.DESCONHECIDO;

    @Column(name = "prefixo_veiculo", length = 10)
    private String prefixoVeiculo;

    /**
     * Extremos da faixa de espera, em minutos.
     *
     * <p>Quando a previsao vem do modelo de Machine Learning, anunciar um numero
     * unico daria ao cidadao uma precisao que o modelo nao tem. O modelo publica
     * entao uma faixa calibrada por predicao conformal, com cobertura verificada
     * de 80%: a espera real cai dentro dela em 4 de cada 5 vezes.</p>
     *
     * <p>Ficam nulos quando a previsao vem da telemetria ou do quadro publicado,
     * que sao pontuais por natureza.</p>
     */
    @PositiveOrZero(message = "O limite inferior da faixa não pode ser negativo")
    @Column(name = "faixa_min_minutos")
    private Integer faixaMinMinutos;

    @PositiveOrZero(message = "O limite superior da faixa não pode ser negativo")
    @Column(name = "faixa_max_minutos")
    private Integer faixaMaxMinutos;

    @NotNull
    @Builder.Default
    @Column(name = "gerada_em", nullable = false)
    private LocalDateTime geradaEm = LocalDateTime.now();

    /** O onibus ja esta na parada: o totem exibe a mensagem O onibus esta aqui. */
    public boolean onibusNoPonto() {
        return minutosEstimados != null && minutosEstimados <= 0;
    }

    /**
     * Veiculo a menos de 3 minutos. O totem promove essa previsao ao card
     * principal do carrossel por 10 segundos, com mapa e dados detalhados.
     */
    public boolean emergencia() {
        return minutosEstimados != null && minutosEstimados < MINUTOS_EMERGENCIA;
    }

    /** Previsao velha demais para continuar na tela. */
    public boolean expirada() {
        return geradaEm == null
                || geradaEm.isBefore(LocalDateTime.now().minusMinutes(MINUTOS_VALIDADE));
    }

    /** Indica se a previsao traz uma faixa de espera, e nao apenas um numero. */
    public boolean possuiFaixa() {
        return faixaMinMinutos != null && faixaMaxMinutos != null;
    }

    /**
     * Texto curto da faixa para a tela do totem, por exemplo {@code "8 a 25 min"}.
     * Devolve {@code null} quando nao ha faixa, para que a tela mostre so o numero.
     */
    public String faixaFormatada() {
        if (!possuiFaixa()) {
            return null;
        }
        return faixaMinMinutos + " a " + faixaMaxMinutos + " min";
    }

    /**
     * Frase lida em voz alta pelo totem quando o cidadao aciona o botao tatil.
     * Escrita por extenso, sem abreviacao, para o sintetizador de voz.
     *
     * <p>Quando a previsao e uma estimativa com faixa, o totem anuncia a faixa em
     * vez do numero: prometer precisao que o modelo nao tem seria enganar quem
     * depende da informacao para decidir se espera ou procura outro caminho.</p>
     */
    public String textoAcessivel() {
        String nomeLinha = linha != null ? linha.getCodigo() : "linha";
        if (onibusNoPonto()) {
            return "Atenção. O ônibus da linha " + nomeLinha + " está no ponto agora.";
        }
        if (origem != null && origem.estimada() && possuiFaixa()) {
            return "O ônibus da linha " + nomeLinha + " deve chegar entre "
                    + faixaMinMinutos + " e " + faixaMaxMinutos
                    + " minutos. Este horário é uma estimativa.";
        }
        String unidade = minutosEstimados == 1 ? " minuto." : " minutos.";
        String frase = "O ônibus da linha " + nomeLinha + " chega em "
                + minutosEstimados + unidade;
        if (origem != null && origem.estimada()) {
            frase += " Este horário é uma estimativa.";
        }
        return frase;
    }
}
