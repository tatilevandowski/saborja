package br.com.saborja.repository;

import br.com.saborja.domain.enums.StatusPagamento;
import br.com.saborja.domain.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    Optional<Pagamento> findByPedidoId(Long pedidoId);

    List<Pagamento> findByStatus(StatusPagamento status);
}
