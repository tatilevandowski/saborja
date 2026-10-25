package br.com.saborja.repository;

import br.com.saborja.domain.model.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    List<Endereco> findByUsuarioId(Long usuarioId);

    Optional<Endereco> findByUsuarioIdAndPrincipalTrue(Long usuarioId);
}
