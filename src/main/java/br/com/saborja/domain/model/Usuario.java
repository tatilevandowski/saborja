package br.com.saborja.domain.model;

import br.com.saborja.domain.enums.Perfil;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pessoa que acessa a plataforma: cliente, responsável por restaurante,
 * entregador ou administrador (diferenciados pelo {@link Perfil}).
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** Hash BCrypt da senha; a senha em texto puro nunca é persistida. */
    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Column(length = 20)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Perfil perfil;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    /** 1:N — um usuário possui vários endereços de entrega. */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Endereco> enderecos = new ArrayList<>();

    /** 1:N — pedidos feitos por este usuário como cliente. */
    @OneToMany(mappedBy = "cliente")
    private List<Pedido> pedidos = new ArrayList<>();

    public Usuario(String nome, String email, String senhaHash, String telefone, Perfil perfil) {
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
        this.telefone = telefone;
        this.perfil = perfil;
    }

    @PrePersist
    void aoPersistir() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }

    public void adicionarEndereco(Endereco endereco) {
        endereco.setUsuario(this);
        enderecos.add(endereco);
    }

    public void removerEndereco(Endereco endereco) {
        enderecos.remove(endereco);
        endereco.setUsuario(null);
    }

    /** Exclusão lógica (RF02). */
    public void desativar() {
        this.ativo = false;
    }
}
