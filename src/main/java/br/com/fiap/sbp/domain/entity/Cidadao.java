package br.com.fiap.sbp.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cidadão cadastrado no aplicativo do S.B.P.
 *
 * <p>Guarda apenas o mínimo necessário para personalizar a jornada e as
 * preferências de acessibilidade. Nenhum dado sensível é armazenado: o projeto
 * segue a orientação da Global Solution de não usar dados pessoais reais, e os
 * registros de exemplo são fictícios.</p>
 */
@Entity
@Table(name = "tb_cidadao", indexes = @Index(name = "ix_cidadao_email", columnList = "email", unique = true))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cidadao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 120)
    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(max = 150)
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Builder.Default
    @Column(name = "prefere_alto_contraste", nullable = false)
    private boolean prefereAltoContraste = false;

    @Builder.Default
    @Column(name = "prefere_leitor_tela", nullable = false)
    private boolean prefereLeitorTela = false;

    @Builder.Default
    @Column(name = "prefere_alerta_vibratorio", nullable = false)
    private boolean prefereAlertaVibratorio = true;

    @NotNull
    @Builder.Default
    @Column(name = "cadastrado_em", nullable = false)
    private LocalDateTime cadastradoEm = LocalDateTime.now();

    @Builder.Default
    @OneToMany(mappedBy = "cidadao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RotinaProgramada> rotinas = new ArrayList<>();

    /**
     * Indica se o cidadão usa algum recurso assistivo. O aplicativo e o totem
     * ajustam a interface a partir dessa informação.
     */
    public boolean usaRecursoAssistivo() {
        return prefereAltoContraste || prefereLeitorTela;
    }

    /** Mantém os dois lados da associação coerentes. */
    public void adicionarRotina(RotinaProgramada rotina) {
        rotinas.add(rotina);
        rotina.setCidadao(this);
    }
}
