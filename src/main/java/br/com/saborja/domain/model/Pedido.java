package br.com.saborja.domain.model;

import br.com.saborja.domain.enums.StatusPagamento;
import br.com.saborja.domain.enums.StatusPedido;
import br.com.saborja.domain.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Pedido de um cliente a um único restaurante. */
@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPedido status = StatusPedido.CRIADO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "taxa_entrega", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxaEntrega = BigDecimal.ZERO;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(length = 255)
    private String observacao;

    /** N:1 — cliente que fez o pedido. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    /** N:1 — restaurante que recebe o pedido. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurante_id", nullable = false)
    private Restaurante restaurante;

    /** N:1 — endereço onde o pedido será entregue. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "endereco_entrega_id", nullable = false)
    private Endereco enderecoEntrega;

    /** N:1 (opcional) — entregador atribuído quando o pedido fica pronto. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entregador_id")
    private Entregador entregador;

    /** 1:N (composição) — itens do pedido. */
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    /** 1:1 — pagamento do pedido (lado inverso; FK fica em pagamentos.pedido_id). */
    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private Pagamento pagamento;

    /** 1:0..1 — avaliação feita após a entrega (lado inverso). */
    @OneToOne(mappedBy = "pedido")
    private Avaliacao avaliacao;

    public Pedido(Usuario cliente, Restaurante restaurante, Endereco enderecoEntrega, String observacao) {
        if (!Boolean.TRUE.equals(restaurante.getAberto())) {
            throw new RegraNegocioException("O restaurante está fechado e não aceita pedidos.");
        }
        this.cliente = cliente;
        this.restaurante = restaurante;
        this.enderecoEntrega = enderecoEntrega;
        this.observacao = observacao;
        this.taxaEntrega = restaurante.getTaxaEntrega();
        this.valorTotal = this.taxaEntrega;
    }

    @PrePersist
    void aoPersistir() {
        if (dataHora == null) {
            dataHora = LocalDateTime.now();
        }
    }

    /** Adiciona um item gravando o preço vigente do produto (RF08). */
    public ItemPedido adicionarItem(Produto produto, int quantidade, String observacao) {
        if (status != StatusPedido.CRIADO) {
            throw new RegraNegocioException("Itens só podem ser alterados em pedidos com status CRIADO.");
        }
        if (!Boolean.TRUE.equals(produto.getDisponivel())) {
            throw new RegraNegocioException("Produto indisponível: " + produto.getNome());
        }
        if (!Objects.equals(produto.getRestaurante().getId(), restaurante.getId())) {
            throw new RegraNegocioException("O produto não pertence ao restaurante do pedido.");
        }
        ItemPedido item = new ItemPedido(this, produto, quantidade, observacao);
        itens.add(item);
        calcularTotal();
        return item;
    }

    /** Recalcula subtotal (soma dos itens) e total (subtotal + taxa de entrega). */
    public BigDecimal calcularTotal() {
        this.subtotal = itens.stream()
                .map(ItemPedido::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.valorTotal = subtotal.add(taxaEntrega);
        return valorTotal;
    }

    /** Vincula o pagamento ao pedido, mantendo os dois lados da associação. */
    public void definirPagamento(Pagamento pagamento) {
        pagamento.setPedido(this);
        this.pagamento = pagamento;
    }

    /** Avança o status respeitando o fluxo definido em {@link StatusPedido} (RF09). */
    public void atualizarStatus(StatusPedido novo) {
        if (novo == StatusPedido.CANCELADO) {
            cancelar();
            return;
        }
        if (!status.podeAvancarPara(novo)) {
            throw new RegraNegocioException("Transição inválida: %s -> %s".formatted(status, novo));
        }
        if (novo == StatusPedido.SAIU_PARA_ENTREGA && entregador == null) {
            throw new RegraNegocioException("Atribua um entregador antes de enviar o pedido.");
        }
        this.status = novo;
    }

    /** Atribui o entregador a um pedido em preparo (RF12). */
    public void atribuirEntregador(Entregador entregador) {
        if (status != StatusPedido.EM_PREPARO) {
            throw new RegraNegocioException("Só é possível atribuir entregador a pedidos EM_PREPARO.");
        }
        if (!Boolean.TRUE.equals(entregador.getDisponivel())) {
            throw new RegraNegocioException("Entregador indisponível.");
        }
        this.entregador = entregador;
    }

    /** Cancela o pedido e estorna pagamento já aprovado (RF10). */
    public void cancelar() {
        if (!status.permiteCancelamento()) {
            throw new RegraNegocioException("Pedido com status " + status + " não pode ser cancelado.");
        }
        this.status = StatusPedido.CANCELADO;
        if (pagamento != null && pagamento.getStatus() == StatusPagamento.APROVADO) {
            pagamento.estornar();
        }
    }
}
