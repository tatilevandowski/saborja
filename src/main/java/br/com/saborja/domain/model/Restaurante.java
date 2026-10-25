package br.com.saborja.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Estabelecimento que publica um cardápio e recebe pedidos. */
@Entity
@Table(name = "restaurantes")
@Getter
@Setter
@NoArgsConstructor
public class Restaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_fantasia", nullable = false, length = 120)
    private String nomeFantasia;

    @Column(nullable = false, unique = true, length = 18)
    private String cnpj;

    @Column(length = 20)
    private String telefone;

    @Column(name = "taxa_entrega", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxaEntrega = BigDecimal.ZERO;

    @Column(name = "tempo_medio_preparo_min", nullable = false)
    private Integer tempoMedioPreparoMin;

    @Column(nullable = false)
    private Boolean aberto = false;

    /** N:1 — usuário com perfil RESTAURANTE responsável pelo estabelecimento. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Usuario responsavel;

    /** 1:1 — endereço do restaurante (salvo/removido junto com ele). */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "endereco_id", nullable = false, unique = true)
    private Endereco endereco;

    /** N:M — categorias de culinária (tabela de junção restaurante_categoria). */
    @ManyToMany
    @JoinTable(
            name = "restaurante_categoria",
            joinColumns = @JoinColumn(name = "restaurante_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id"))
    private Set<Categoria> categorias = new HashSet<>();

    /** 1:N — cardápio. */
    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Produto> produtos = new ArrayList<>();

    public Restaurante(String nomeFantasia, String cnpj, String telefone, BigDecimal taxaEntrega,
                       Integer tempoMedioPreparoMin, Usuario responsavel, Endereco endereco) {
        this.nomeFantasia = nomeFantasia;
        this.cnpj = cnpj;
        this.telefone = telefone;
        this.taxaEntrega = taxaEntrega;
        this.tempoMedioPreparoMin = tempoMedioPreparoMin;
        this.responsavel = responsavel;
        this.endereco = endereco;
    }

    public void abrir() {
        this.aberto = true;
    }

    public void fechar() {
        this.aberto = false;
    }

    public void adicionarProduto(Produto produto) {
        produto.setRestaurante(this);
        produtos.add(produto);
    }

    public void removerProduto(Produto produto) {
        produtos.remove(produto);
        produto.setRestaurante(null);
    }

    public void adicionarCategoria(Categoria categoria) {
        categorias.add(categoria);
        categoria.getRestaurantes().add(this);
    }

    public void removerCategoria(Categoria categoria) {
        categorias.remove(categoria);
        categoria.getRestaurantes().remove(this);
    }
}
