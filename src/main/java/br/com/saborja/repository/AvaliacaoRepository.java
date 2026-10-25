package br.com.saborja.repository;

import br.com.saborja.domain.model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    boolean existsByPedidoId(Long pedidoId);

    List<Avaliacao> findByPedidoRestauranteIdOrderByDataAvaliacaoDesc(Long restauranteId);

    /** RF13 — nota média do restaurante (null se ainda não houver avaliações). */
    @Query("select avg(a.nota) from Avaliacao a where a.pedido.restaurante.id = :restauranteId")
    Double calcularMediaPorRestaurante(@Param("restauranteId") Long restauranteId);
}
