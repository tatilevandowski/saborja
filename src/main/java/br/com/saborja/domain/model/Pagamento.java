package br.com.saborja.domain.model;

import br.com.saborja.domain.enums.FormaPagamento;
import br.com.saborja.domain.enums.StatusPagamento;
import br.com.saborja.domain.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Pagamento de um pedido (RF11). */
@Entity
@Table(name = "pagamentos")
@Getter
@Setter
@NoArgsConstructor
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FormaPagamento forma;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPagamento status = StatusPagamento.PENDENTE;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_pagamento")
    private LocalDateTime dataPagamento;

    @Column(name = "codigo_transacao", length = 100)
    private String codigoTransacao;

    /** 1:1 — lado dono da associação; um pagamento por pedido. */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    public Pagamento(FormaPagamento forma, BigDecimal valor) {
        this.forma = forma;
        this.valor = valor;
    }

    public void aprovar(String codigoTransacao) {
        if (status != StatusPagamento.PENDENTE) {
            throw new RegraNegocioException("Só pagamentos PENDENTES podem ser aprovados.");
        }
        this.status = StatusPagamento.APROVADO;
        this.codigoTransacao = codigoTransacao;
        this.dataPagamento = LocalDateTime.now();
    }

    public void recusar() {
        if (status != StatusPagamento.PENDENTE) {
            throw new RegraNegocioException("Só pagamentos PENDENTES podem ser recusados.");
        }
        this.status = StatusPagamento.RECUSADO;
    }

    public void estornar() {
        if (status != StatusPagamento.APROVADO) {
            throw new RegraNegocioException("Só pagamentos APROVADOS podem ser estornados.");
        }
        this.status = StatusPagamento.ESTORNADO;
    }
}
