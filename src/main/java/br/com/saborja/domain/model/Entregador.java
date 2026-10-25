package br.com.saborja.domain.model;

import br.com.saborja.domain.enums.TipoVeiculo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Dados profissionais do usuário com perfil ENTREGADOR. */
@Entity
@Table(name = "entregadores")
@Getter
@Setter
@NoArgsConstructor
public class Entregador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoVeiculo veiculo;

    @Column(length = 8)
    private String placa;

    @Column(nullable = false)
    private Boolean disponivel = false;

    /** 1:1 — conta de usuário do entregador. */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    public Entregador(String cpf, TipoVeiculo veiculo, String placa, Usuario usuario) {
        this.cpf = cpf;
        this.veiculo = veiculo;
        this.placa = placa;
        this.usuario = usuario;
    }

    public void alterarDisponibilidade(boolean disponivel) {
        this.disponivel = disponivel;
    }
}
