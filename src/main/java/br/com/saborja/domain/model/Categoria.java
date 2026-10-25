package br.com.saborja.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/** Categoria de culinária (Pizza, Japonesa, Lanches...). */
@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String nome;

    @Column(length = 255)
    private String descricao;

    /** N:M — lado inverso; o dono da associação é {@link Restaurante#getCategorias()}. */
    @ManyToMany(mappedBy = "categorias")
    private Set<Restaurante> restaurantes = new HashSet<>();

    public Categoria(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }
}
