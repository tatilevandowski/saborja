package br.com.saborja.repository;

import br.com.saborja.domain.model.Entregador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EntregadorRepository extends JpaRepository<Entregador, Long> {

    Optional<Entregador> findByUsuarioId(Long usuarioId);

    Optional<Entregador> findByCpf(String cpf);

    /** RF12 — entregadores disponíveis para atribuição. */
    List<Entregador> findByDisponivelTrue();
}
