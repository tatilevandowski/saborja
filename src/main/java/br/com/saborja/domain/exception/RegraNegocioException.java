package br.com.saborja.domain.exception;

/** Lançada quando uma operação viola uma regra de negócio do domínio. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
