package br.com.fiap.sbp.domain.entity;

import br.com.fiap.sbp.domain.enums.StatusTotem;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Totem físico instalado na calçada.
 *
 * <p>É o equipamento que sustenta o pilar de inclusão do S.B.P.: o cidadão sem
 * celular, sem bateria ou sem dados móveis continua tendo a mesma informação.
 * O tokenQrCode liga o mundo físico ao digital: ao ler o QR Code, o passageiro
 * abre no celular exatamente o ponto em que está.</p>
 */
@Entity
@Table(name = "tb_totem", indexes = {
        @Index(name = "ix_totem_codigo", columnList = "codigo", unique = true),
        @Index(name = "ix_totem_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Totem {

    /** Depois desse tempo sem sincronizar, o totem é considerado sem comunicação. */
    public static final int MINUTOS_LIMITE_SINCRONIZACAO = 15;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O código do totem é obrigatório")
    @Size(max = 20)
    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @NotNull(message = "O totem precisa estar vinculado a uma parada")
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parada_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_totem_parada"))
    private Parada parada;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusTotem status = StatusTotem.ATIVO;

    @Size(max = 64)
    @Column(name = "token_qr_code", length = 64, unique = true)
    private String tokenQrCode;

    @Size(max = 20)
    @Column(name = "versao_firmware", length = 20)
    private String versaoFirmware;

    @Builder.Default
    @Column(name = "possui_audio_tts", nullable = false)
    private boolean possuiAudioTts = true;

    @Builder.Default
    @Column(name = "possui_braille", nullable = false)
    private boolean possuiBraille = true;

    @Column(name = "instalado_em")
    private LocalDateTime instaladoEm;

    @Column(name = "ultima_sincronizacao")
    private LocalDateTime ultimaSincronizacao;

    /** Registra que o totem acabou de se comunicar com o backend. */
    public void sincronizar() {
        this.ultimaSincronizacao = LocalDateTime.now();
        if (this.status == StatusTotem.SEM_COMUNICACAO) {
            this.status = StatusTotem.ATIVO;
        }
    }

    /** Minutos decorridos desde a última sincronização, ou null se nunca sincronizou. */
    public Long minutosDesdeUltimaSincronizacao() {
        if (ultimaSincronizacao == null) {
            return null;
        }
        return Duration.between(ultimaSincronizacao, LocalDateTime.now()).toMinutes();
    }

    /**
     * Um totem ativo que parou de se comunicar continua marcado como ativo no
     * banco, mas já não exibe informação confiável: a operação precisa ser avisada.
     */
    public boolean precisaAtencao() {
        if (status == StatusTotem.MANUTENCAO || status == StatusTotem.SEM_COMUNICACAO) {
            return true;
        }
        Long minutos = minutosDesdeUltimaSincronizacao();
        return status == StatusTotem.ATIVO
                && (minutos == null || minutos > MINUTOS_LIMITE_SINCRONIZACAO);
    }

    /** Indica se o totem atende integralmente a Lei Brasileira de Inclusão. */
    public boolean plenamenteAcessivel() {
        return possuiAudioTts && possuiBraille
                && parada != null && parada.isAcessivelCadeirante();
    }
}
