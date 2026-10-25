package br.com.saborja.domain.enums;

/**
 * Ciclo de vida do pedido:
 * CRIADO -> CONFIRMADO -> EM_PREPARO -> SAIU_PARA_ENTREGA -> ENTREGUE.
 * CANCELADO é permitido apenas até EM_PREPARO.
 */
public enum StatusPedido {
    CRIADO,
    CONFIRMADO,
    EM_PREPARO,
    SAIU_PARA_ENTREGA,
    ENTREGUE,
    CANCELADO;

    /** Indica se o pedido ainda pode ser cancelado neste status. */
    public boolean permiteCancelamento() {
        return this == CRIADO || this == CONFIRMADO || this == EM_PREPARO;
    }

    /** Indica se a transição para {@code novo} respeita a ordem do fluxo. */
    public boolean podeAvancarPara(StatusPedido novo) {
        if (novo == CANCELADO) {
            return permiteCancelamento();
        }
        return switch (this) {
            case CRIADO -> novo == CONFIRMADO;
            case CONFIRMADO -> novo == EM_PREPARO;
            case EM_PREPARO -> novo == SAIU_PARA_ENTREGA;
            case SAIU_PARA_ENTREGA -> novo == ENTREGUE;
            case ENTREGUE, CANCELADO -> false;
        };
    }
}
