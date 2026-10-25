package br.com.saborja.repository;

import br.com.saborja.domain.model.Restaurante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {

    Optional<Restaurante> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);

    /** RF07 — restaurantes abertos, paginados. */
    Page<Restaurante> findByAbertoTrue(Pageable pageable);

    /** RF07 — busca por nome. */
    Page<Restaurante> findByNomeFantasiaContainingIgnoreCase(String nome, Pageable pageable);

    /** RF07 — filtro por categoria (navega pela associação N:M). */
    Page<Restaurante> findByAbertoTrueAndCategorias_NomeIgnoreCase(String categoria, Pageable pageable);

    List<Restaurante> findByResponsavelId(Long responsavelId);
}
