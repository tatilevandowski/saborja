package br.com.saborja.domain.model;

import br.com.saborja.domain.enums.StatusPedido;
import br.com.saborja.domain.exception.RegraNegocioException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Avaliação de um pedido entregue (RF13). */
@Entity
@Table(name = "avaliacoes")
@Getter
@Setter
@NoArgsConstructor
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer nota;

    @Column(length = 500)
    private String comentario;

    @Column(name = "data_avaliacao", nullable = false)
    private LocalDateTime dataAvaliacao;

    /** 1:1 — lado dono; no máximo uma avaliação por pedido. */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    public Avaliacao(Pedido pedido, int nota, String comentario) {
        if (pedido.getStatus() != StatusPedido.ENTREGUE) {
            throw new RegraNegocioException("Só pedidos ENTREGUES podem ser avaliados.");
        }
        if (nota < 1 || nota > 5) {
            throw new RegraNegocioException("A nota deve estar entre 1 e 5.");
        }
        this.pedido = pedido;
        this.nota = nota;
        this.comentario = comentario;
        pedido.setAvaliacao(this);
    }

    @PrePersist
    void aoPersistir() {
        if (dataAvaliacao == null) {
            dataAvaliacao = LocalDateTime.now();
        }
    }
}
