# SaborJá — API de pedidos e entregas para restaurantes

Back-end do **SaborJá**, plataforma de delivery próprio para restaurantes locais.
Esta é a **Etapa 1**: modelagem de domínio e camada de persistência (JPA + Spring Data JPA).

## Stack

- Java 21 · Spring Boot 3.3
- Spring Data JPA / Hibernate · Bean Validation · Lombok
- H2 (desenvolvimento e testes) · PostgreSQL 16 (produção)
- Maven

## Modelo de domínio

10 entidades JPA (`src/main/java/br/com/saborja/domain/model`):

| Entidade | Tabela | Relacionamentos principais |
| --- | --- | --- |
| Usuario | usuarios | 1:N Endereco, 1:N Pedido, 1:N Restaurante, 1:1 Entregador |
| Endereco | enderecos | N:1 Usuario |
| Categoria | categorias | N:M Restaurante |
| Restaurante | restaurantes | 1:1 Endereco, N:M Categoria, 1:N Produto, 1:N Pedido |
| Produto | produtos | N:1 Restaurante |
| Entregador | entregadores | 1:1 Usuario, 1:N Pedido |
| Pedido | pedidos | 1:N ItemPedido, 1:1 Pagamento, 1:0..1 Avaliacao |
| ItemPedido | itens_pedido | N:1 Pedido, N:1 Produto |
| Pagamento | pagamentos | 1:1 Pedido |
| Avaliacao | avaliacoes | 1:1 Pedido |

Enumerações: `Perfil`, `StatusPedido`, `FormaPagamento`, `StatusPagamento`, `TipoVeiculo`.
Repositórios `JpaRepository` em `src/main/java/br/com/saborja/repository`.

Diagrama de classes UML: [`docs/diagrama-classes.png`](docs/diagrama-classes.png)
(fonte Graphviz em `docs/diagrama-classes.dot`; regenerar com
`dot -Tpng -Gdpi=130 docs/diagrama-classes.dot -o docs/diagrama-classes.png`).

## Como executar

Pré-requisitos: JDK 21 e Maven 3.9+.

```bash
# Testes da camada de persistência (H2 em memória)
mvn test

# Aplicação em modo desenvolvimento (H2; console em http://localhost:8080/h2-console)
mvn spring-boot:run
```

JDBC URL do console H2: `jdbc:h2:mem:saborja` · usuário `sa` · sem senha.

### Com PostgreSQL (perfil `prod`)

```bash
docker compose up -d
export DB_PASSWORD=saborja_dev
SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run
```

Variáveis de ambiente: `DB_URL` (padrão `jdbc:postgresql://localhost:5432/saborja`),
`DB_USER` (padrão `saborja`), `DB_PASSWORD` (obrigatória).

## Estrutura

```
src/main/java/br/com/saborja
├── SaborjaApplication.java
├── domain
│   ├── enums/        # enumerações persistidas como texto
│   ├── exception/    # RegraNegocioException
│   └── model/        # entidades @Entity
└── repository/       # interfaces JpaRepository
src/test/java/br/com/saborja/repository
└── PersistenciaDominioTest.java   # @DataJpaTest — 7 cenários
docs/
└── diagrama-classes.{dot,png,svg}
```

## Próximas etapas

- Etapa 2: camadas `service`, `dto` e `controller` (API REST `/api/v1`), tratamento de erros (RFC 9457)
- Autenticação JWT com Spring Security e senhas em BCrypt
- Documentação OpenAPI / Swagger UI
