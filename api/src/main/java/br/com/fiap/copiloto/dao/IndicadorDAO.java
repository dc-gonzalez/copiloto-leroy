package br.com.fiap.copiloto.dao;

import br.com.fiap.copiloto.dto.IndicadoresDTO;
import br.com.fiap.copiloto.exception.DadosIndisponiveisException;
import br.com.fiap.copiloto.infra.ConnectionFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Read-only aggregate queries for the store associate panel. */
public class IndicadorDAO {

    public IndicadoresDTO carregar() {
        try (Connection con = ConnectionFactory.getConnection()) {
            int projetosHoje = inteiro(con, "SELECT COUNT(*) FROM cl_projeto WHERE dt_criacao >= TRUNC(SYSDATE)");

            BigDecimal ticketMedio = decimal(con, """
                    SELECT ROUND(AVG(total), 2)
                      FROM (SELECT i.id_projeto, SUM(i.qt_item * p.vl_preco) AS total
                              FROM cl_item_projeto i
                              JOIN cl_produto p ON p.id_produto = i.id_produto
                             GROUP BY i.id_projeto)
                    """);

            int chamadosAbertos = inteiro(con,
                    "SELECT COUNT(*) FROM cl_chamado WHERE st_chamado IN ('ABERTO', 'A_CAMINHO')");

            BigDecimal minutosMedio = decimal(con, """
                    SELECT ROUND(AVG(EXTRACT(DAY FROM (dt_aceite - dt_abertura)) * 1440
                                   + EXTRACT(HOUR FROM (dt_aceite - dt_abertura)) * 60
                                   + EXTRACT(MINUTE FROM (dt_aceite - dt_abertura))
                                   + EXTRACT(SECOND FROM (dt_aceite - dt_abertura)) / 60), 1)
                      FROM cl_chamado
                     WHERE dt_aceite IS NOT NULL
                    """);

            int rupturasHoje = inteiro(con, "SELECT COUNT(*) FROM cl_ruptura WHERE dt_registro >= TRUNC(SYSDATE)");

            return new IndicadoresDTO(projetosHoje, ticketMedio, chamadosAbertos,
                    minutosMedio.doubleValue(), rupturasHoje,
                    rupturasPorProduto(con), chamadosPorSecao(con), produtosMaisPedidos(con));
        } catch (SQLException e) {
            throw new DadosIndisponiveisException("carregar indicadores", e);
        }
    }

    private List<IndicadoresDTO.RupturaPorProduto> rupturasPorProduto(Connection con) throws SQLException {
        String sql = """
                SELECT p.cd_sku, p.nm_produto, c.nr_corredor, COUNT(*) AS ocorrencias,
                       (SELECT r2.tp_desfecho FROM cl_ruptura r2
                         WHERE r2.id_produto = p.id_produto
                         ORDER BY r2.dt_registro DESC FETCH FIRST 1 ROW ONLY) AS ultimo_desfecho,
                       e.qt_gondola, e.qt_sistema
                  FROM cl_ruptura r
                  JOIN cl_produto p ON p.id_produto = r.id_produto
                  JOIN cl_corredor c ON c.id_corredor = p.id_corredor
                  JOIN cl_estoque e ON e.id_produto = p.id_produto
                 GROUP BY p.id_produto, p.cd_sku, p.nm_produto, c.nr_corredor, e.qt_gondola, e.qt_sistema
                 ORDER BY CASE WHEN e.qt_gondola = 0 THEN 0 ELSE 1 END, ocorrencias DESC
                 FETCH FIRST 8 ROWS ONLY
                """;
        List<IndicadoresDTO.RupturaPorProduto> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new IndicadoresDTO.RupturaPorProduto(rs.getString(1), rs.getString(2), rs.getInt(3),
                        rs.getInt(4), rs.getString(5), rs.getInt(6) == 0, rs.getInt(7)));
            }
        }
        return lista;
    }

    private List<IndicadoresDTO.ChamadosPorSecao> chamadosPorSecao(Connection con) throws SQLException {
        String sql = """
                SELECT s.nm_secao, s.ds_cor, COUNT(ch.id_chamado) AS total
                  FROM cl_chamado ch
                  JOIN cl_corredor c ON c.id_corredor = ch.id_corredor
                  JOIN cl_secao s ON s.id_secao = c.id_secao
                 GROUP BY s.nm_secao, s.ds_cor
                HAVING COUNT(ch.id_chamado) > 0
                 ORDER BY total DESC
                """;
        List<IndicadoresDTO.ChamadosPorSecao> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new IndicadoresDTO.ChamadosPorSecao(rs.getString(1), rs.getString(2), rs.getInt(3)));
            }
        }
        return lista;
    }

    private List<IndicadoresDTO.ProdutoMaisPedido> produtosMaisPedidos(Connection con) throws SQLException {
        String sql = """
                SELECT p.cd_sku, p.nm_produto, COUNT(DISTINCT i.id_projeto) AS projetos, SUM(i.qt_item) AS unidades
                  FROM cl_item_projeto i
                  JOIN cl_produto p ON p.id_produto = i.id_produto
                 GROUP BY p.cd_sku, p.nm_produto
                 ORDER BY projetos DESC, unidades DESC
                 FETCH FIRST 5 ROWS ONLY
                """;
        List<IndicadoresDTO.ProdutoMaisPedido> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new IndicadoresDTO.ProdutoMaisPedido(rs.getString(1), rs.getString(2), rs.getInt(3),
                        rs.getInt(4)));
            }
        }
        return lista;
    }

    private int inteiro(Connection con, String sql) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private BigDecimal decimal(Connection con, String sql) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next();
            BigDecimal valor = rs.getBigDecimal(1);
            return valor == null ? BigDecimal.ZERO : valor;
        }
    }
}
