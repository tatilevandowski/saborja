package br.com.saborja.repository;

import br.com.saborja.domain.enums.StatusPedido;
import br.com.saborja.domain.model.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /** RF14 — histórico do cliente, do mais recente ao mais antigo. */
    Page<Pedido> findByClienteIdOrderByDataHoraDesc(Long clienteId, Pageable pageable);

    /** RF14 — pedidos recebidos pelo restaurante, por status. */
    List<Pedido> findByRestauranteIdAndStatusOrderByDataHoraAsc(Long restauranteId, StatusPedido status);

    /** RF14 — pedidos recebidos pelo restaurante em um período. */
    List<Pedido> findByRestauranteIdAndDataHoraBetween(Long restauranteId, LocalDateTime inicio, LocalDateTime fim);

    /** RF12 — entregas em andamento de um entregador. */
    List<Pedido> findByEntregadorIdAndStatus(Long entregadorId, StatusPedido status);

    /** RF15 — faturamento de pedidos entregues no período (null se não houver pedidos). */
    @Query("""
            select sum(p.valorTotal)
              from Pedido p
             where p.restaurante.id = :restauranteId
               and p.status = br.com.saborja.domain.enums.StatusPedido.ENTREGUE
               and p.dataHora between :inicio and :fim
            """)
    BigDecimal somarFaturamento(@Param("restauranteId") Long restauranteId,
                                @Param("inicio") LocalDateTime inicio,
                                @Param("fim") LocalDateTime fim);

    /** RF15 — quantidade de pedidos em um status no período. */
    long countByRestauranteIdAndStatusAndDataHoraBetween(Long restauranteId, StatusPedido status,
                                                        LocalDateTime inicio, LocalDateTime fim);
}
