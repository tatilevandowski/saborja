package br.com.saborja.repository;

import br.com.saborja.domain.enums.FormaPagamento;
import br.com.saborja.domain.enums.Perfil;
import br.com.saborja.domain.enums.StatusPagamento;
import br.com.saborja.domain.enums.StatusPedido;
import br.com.saborja.domain.enums.TipoVeiculo;
import br.com.saborja.domain.exception.RegraNegocioException;
import br.com.saborja.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Valida o mapeamento JPA das 10 entidades e as consultas dos repositórios,
 * persistindo um grafo completo: cliente → pedido → itens → pagamento → avaliação.
 */
@DataJpaTest
class PersistenciaDominioTest {

    @Autowired TestEntityManager em;
    @Autowired UsuarioRepository usuarios;
    @Autowired EnderecoRepository enderecos;
    @Autowired CategoriaRepository categorias;
    @Autowired RestauranteRepository restaurantes;
    @Autowired ProdutoRepository produtos;
    @Autowired EntregadorRepository entregadores;
    @Autowired PedidoRepository pedidos;
    @Autowired ItemPedidoRepository itens;
    @Autowired PagamentoRepository pagamentos;
    @Autowired AvaliacaoRepository avaliacoes;

    private Usuario cliente;
    private Endereco casa;
    private Restaurante pizzaria;
    private Produto margherita;
    private Produto refrigerante;
    private Entregador entregador;

    @BeforeEach
    void montarCenario() {
        cliente = new Usuario("Ana Souza", "ana@email.com", "$2a$10$hash", "47999990000", Perfil.CLIENTE);
        casa = new Endereco("Rua das Flores", "100", "Ap 12", "Centro", "Joinville", "SC", "89201-000");
        casa.setPrincipal(true);
        cliente.adicionarEndereco(casa);
        usuarios.save(cliente);

        Usuario dono = usuarios.save(
                new Usuario("Carlos Lima", "carlos@pizzaria.com", "$2a$10$hash", "4733330000", Perfil.RESTAURANTE));
        Categoria pizza = categorias.save(new Categoria("Pizza", "Pizzarias"));
        Categoria italiana = categorias.save(new Categoria("Italiana", "Culinária italiana"));

        Endereco endPizzaria = new Endereco("Av. Brasil", "500", null, "América", "Joinville", "SC", "89204-000");
        pizzaria = new Restaurante("Pizzaria Bella", "12.345.678/0001-90", "4733330000",
                new BigDecimal("7.00"), 40, dono, endPizzaria);
        pizzaria.adicionarCategoria(pizza);
        pizzaria.adicionarCategoria(italiana);
        margherita = new Produto("Pizza Margherita", "Molho, mussarela e manjericão", new BigDecimal("49.90"));
        refrigerante = new Produto("Refrigerante 2L", null, new BigDecimal("12.00"));
        pizzaria.adicionarProduto(margherita);
        pizzaria.adicionarProduto(refrigerante);
        pizzaria.abrir();
        restaurantes.save(pizzaria);

        Usuario moto = usuarios.save(
                new Usuario("João Reis", "joao@email.com", "$2a$10$hash", "47988880000", Perfil.ENTREGADOR));
        entregador = new Entregador("123.456.789-00", TipoVeiculo.MOTO, "ABC1D23", moto);
        entregador.alterarDisponibilidade(true);
        entregadores.save(entregador);

        em.flush();
    }

    private Pedido criarPedidoPago() {
        Pedido pedido = new Pedido(cliente, pizzaria, casa, "Sem cebola");
        pedido.adicionarItem(margherita, 2, null);
        pedido.adicionarItem(refrigerante, 1, "Bem gelado");
        Pagamento pag = new Pagamento(FormaPagamento.PIX, pedido.getValorTotal());
        pedido.definirPagamento(pag);
        pag.aprovar("PIX-0001");
        return pedidos.save(pedido);
    }

    @Test
    void persisteGrafoCompletoECalculaTotais() {
        Pedido salvo = criarPedidoPago();
        em.flush();
        em.clear();

        Pedido lido = pedidos.findById(salvo.getId()).orElseThrow();
        // 2 x 49,90 + 1 x 12,00 = 111,80 ; + 7,00 de taxa = 118,80
        assertThat(lido.getSubtotal()).isEqualByComparingTo("111.80");
        assertThat(lido.getValorTotal()).isEqualByComparingTo("118.80");
        assertThat(lido.getItens()).hasSize(2);
        assertThat(lido.getPagamento().getStatus()).isEqualTo(StatusPagamento.APROVADO);
        assertThat(itens.findByPedidoId(lido.getId())).hasSize(2);
        assertThat(pagamentos.findByPedidoId(lido.getId())).isPresent();
    }

    @Test
    void mapeiaRelacionamentoMuitosParaMuitosEUmParaUm() {
        em.clear();
        var porCategoria = restaurantes.findByAbertoTrueAndCategorias_NomeIgnoreCase("pizza", PageRequest.of(0, 20));
        assertThat(porCategoria.getContent()).extracting(Restaurante::getNomeFantasia).containsExactly("Pizzaria Bella");

        Restaurante lido = restaurantes.findByCnpj("12.345.678/0001-90").orElseThrow();
        assertThat(lido.getCategorias()).hasSize(2);
        assertThat(lido.getEndereco().getCidade()).isEqualTo("Joinville");
        assertThat(entregadores.findByUsuarioId(entregador.getUsuario().getId())).isPresent();
        assertThat(enderecos.findByUsuarioIdAndPrincipalTrue(cliente.getId())).isPresent();
    }

    @Test
    void cardapioPublicoListaApenasProdutosDisponiveis() {
        refrigerante.alterarDisponibilidade(false);
        em.flush();
        List<Produto> cardapio = produtos.findByRestauranteIdAndDisponivelTrueOrderByNomeAsc(pizzaria.getId());
        assertThat(cardapio).extracting(Produto::getNome).containsExactly("Pizza Margherita");
    }

    @Test
    void fluxoCompletoAteAvaliacaoECalculoDeMedia() {
        Pedido pedido = criarPedidoPago();
        pedido.atualizarStatus(StatusPedido.CONFIRMADO);
        pedido.atualizarStatus(StatusPedido.EM_PREPARO);
        pedido.atribuirEntregador(entregador);
        pedido.atualizarStatus(StatusPedido.SAIU_PARA_ENTREGA);
        pedido.atualizarStatus(StatusPedido.ENTREGUE);
        avaliacoes.save(new Avaliacao(pedido, 5, "Chegou quentinha"));
        em.flush();

        assertThat(avaliacoes.calcularMediaPorRestaurante(pizzaria.getId())).isEqualTo(5.0);
        assertThat(avaliacoes.existsByPedidoId(pedido.getId())).isTrue();

        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fim = LocalDateTime.now().plusDays(1);
        assertThat(pedidos.somarFaturamento(pizzaria.getId(), inicio, fim)).isEqualByComparingTo("118.80");
        assertThat(pedidos.countByRestauranteIdAndStatusAndDataHoraBetween(
                pizzaria.getId(), StatusPedido.ENTREGUE, inicio, fim)).isEqualTo(1);
        assertThat(pedidos.findByClienteIdOrderByDataHoraDesc(cliente.getId(), PageRequest.of(0, 20))
                .getTotalElements()).isEqualTo(1);
    }

    @Test
    void cancelamentoEstornaPagamentoEBloqueiaAposSaida() {
        Pedido pedido = criarPedidoPago();
        pedido.cancelar();
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CANCELADO);
        assertThat(pedido.getPagamento().getStatus()).isEqualTo(StatusPagamento.ESTORNADO);

        Pedido outro = criarPedidoPago();
        outro.atualizarStatus(StatusPedido.CONFIRMADO);
        outro.atualizarStatus(StatusPedido.EM_PREPARO);
        outro.atribuirEntregador(entregador);
        outro.atualizarStatus(StatusPedido.SAIU_PARA_ENTREGA);
        assertThatThrownBy(outro::cancelar).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void rejeitaTransicaoForaDeOrdemERestauranteFechado() {
        Pedido pedido = criarPedidoPago();
        assertThatThrownBy(() -> pedido.atualizarStatus(StatusPedido.ENTREGUE))
                .isInstanceOf(RegraNegocioException.class);

        pizzaria.fechar();
        assertThatThrownBy(() -> new Pedido(cliente, pizzaria, casa, null))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void emailDuplicadoViolaRestricaoUnique() {
        Usuario duplicado = new Usuario("Outra Ana", "ana@email.com", "$2a$10$hash", null, Perfil.CLIENTE);
        // Com IDENTITY o INSERT ocorre já no save; o flush garante a verificação em qualquer estratégia.
        assertThatThrownBy(() -> {
            usuarios.save(duplicado);
            usuarios.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
