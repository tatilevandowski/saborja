package br.com.saborja.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Endereço de entrega de um cliente ou endereço de um restaurante.
 * Quando pertence a um restaurante, {@code usuario} fica nulo.
 */
@Entity
@Table(name = "enderecos")
@Getter
@Setter
@NoArgsConstructor
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String logradouro;

    @Column(nullable = false, length = 10)
    private String numero;

    @Column(length = 80)
    private String complemento;

    @Column(nullable = false, length = 80)
    private String bairro;

    @Column(nullable = false, length = 80)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String uf;

    @Column(nullable = false, length = 9)
    private String cep;

    @Column(nullable = false)
    private Boolean principal = false;

    /** N:1 — dono do endereço (cliente). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Endereco(String logradouro, String numero, String complemento,
                    String bairro, String cidade, String uf, String cep) {
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.uf = uf;
        this.cep = cep;
    }

    public String formatar() {
        String compl = (complemento == null || complemento.isBlank()) ? "" : ", " + complemento;
        return "%s, %s%s - %s, %s/%s - CEP %s".formatted(logradouro, numero, compl, bairro, cidade, uf, cep);
    }
}
