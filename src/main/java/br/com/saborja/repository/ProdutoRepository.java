package br.com.saborja.repository;

import br.com.saborja.domain.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    /** RF07 — cardápio público: apenas produtos disponíveis. */
    List<Produto> findByRestauranteIdAndDisponivelTrueOrderByNomeAsc(Long restauranteId);

    /** RF06 — cardápio completo, visto pelo restaurante. */
    List<Produto> findByRestauranteIdOrderByNomeAsc(Long restauranteId);
}
