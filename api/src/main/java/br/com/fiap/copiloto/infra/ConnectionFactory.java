package br.com.fiap.copiloto.infra;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Hands out pooled Oracle connections. Callers close the connection as usual
 * (try-with-resources); closing returns it to the pool instead of dropping the
 * TCP session, which matters because the database is far from the API host.
 */
public final class ConnectionFactory {

    private static PoolDataSource pool;

    private ConnectionFactory() {
    }

    public static synchronized Connection getConnection() throws SQLException {
        if (pool == null) {
            PoolDataSource ds = PoolDataSourceFactory.getPoolDataSource();
            ds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
            ds.setURL(Config.dbUrl());
            ds.setUser(Config.dbUsuario());
            ds.setPassword(Config.dbSenha());
            ds.setInitialPoolSize(1);
            ds.setMinPoolSize(1);
            ds.setMaxPoolSize(5);
            ds.setValidateConnectionOnBorrow(true);
            ds.setInactiveConnectionTimeout(300);
            ds.setConnectionWaitDuration(java.time.Duration.ofSeconds(15));
            pool = ds;
        }
        return pool.getConnection();
    }

    public static synchronized void fechar() {
        pool = null;
    }
}
