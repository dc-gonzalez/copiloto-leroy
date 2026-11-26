package br.com.fiap.copiloto.infra;

public final class Config {

    private Config() {
    }

    public static String env(String nome, String padrao) {
        String valor = System.getenv(nome);
        return valor == null || valor.isBlank() ? padrao : valor.trim();
    }

    public static int porta() {
        return Integer.parseInt(env("PORT", "8080"));
    }

    public static String dbUrl() {
        return env("DB_URL", "jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL");
    }

    public static String dbUsuario() {
        return env("DB_USER", "");
    }

    public static String dbSenha() {
        return env("DB_PASSWORD", "");
    }

    public static String openAiChave() {
        return env("OPENAI_API_KEY", "");
    }

    public static String openAiModelo() {
        return env("OPENAI_MODEL", "gpt-5.4-mini");
    }

    /** Comma-separated list of allowed browser origins; "*" allows any. */
    public static String origensPermitidas() {
        return env("CORS_ORIGINS", "*");
    }
}
