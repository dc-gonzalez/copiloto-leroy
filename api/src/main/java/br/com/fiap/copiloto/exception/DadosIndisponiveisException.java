package br.com.fiap.copiloto.exception;

/** Wraps SQLExceptions so resources do not leak JDBC details to the client. */
public class DadosIndisponiveisException extends RuntimeException {

    public DadosIndisponiveisException(String operacao, Throwable causa) {
        super(operacao, causa);
    }
}
