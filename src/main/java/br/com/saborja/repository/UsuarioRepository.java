package br.com.saborja.repository;

import br.com.saborja.domain.enums.Perfil;
import br.com.saborja.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Usuario> findByPerfilAndAtivoTrue(Perfil perfil);
}
