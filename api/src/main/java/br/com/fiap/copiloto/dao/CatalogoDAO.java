package br.com.fiap.copiloto.dao;

import br.com.fiap.copiloto.exception.DadosIndisponiveisException;
import br.com.fiap.copiloto.infra.ConnectionFactory;
import br.com.fiap.copiloto.model.Complemento;
import br.com.fiap.copiloto.model.Corredor;
import br.com.fiap.copiloto.model.Produto;
import br.com.fiap.copiloto.model.ProdutoMostruario;
import br.com.fiap.copiloto.model.ProdutoProntaEntrega;
import br.com.fiap.copiloto.model.Secao;
import br.com.fiap.copiloto.model.TipoAtendimento;
import br.com.fiap.copiloto.model.TipoDisponibilidade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CatalogoDAO {

    public List<Corredor> listarCorredores() {
        String sql = """
                SELECT c.id_corredor, c.nr_corredor, c.id_no, s.id_secao, s.nm_secao, s.ds_cor
                  FROM cl_corredor c
                  JOIN cl_secao s ON s.id_secao = c.id_secao
                 ORDER BY c.nr_corredor
                """;
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Corredor> corredores = new ArrayList<>();
            while (rs.next()) {
                Secao secao = new Secao(rs.getInt("id_secao"), rs.getString("nm_secao"), rs.getString("ds_cor"));
                corredores.add(new Corredor(rs.getInt("id_corredor"), rs.getInt("nr_corredor"), secao, rs.getLong("id_no")));
            }
            return corredores;
        } catch (SQLException e) {
            throw new DadosIndisponiveisException("listar corredores", e);
        }
    }

    public List<Produto> listarProdutos() {
        String sql = """
                SELECT p.id_produto, p.cd_sku, p.nm_produto, p.ds_produto, p.nm_marca, p.fl_marca_propria,
                       p.vl_preco, p.ds_unidade, c.nr_corredor, p.nr_prateleira, p.tp_atendimento,
                       p.tp_disponibilidade, p.ds_palavras_chave,
                       NVL(e.qt_sistema, 0) AS qt_sistema, NVL(e.qt_gondola, 0) AS qt_gondola
                  FROM cl_produto p
                  JOIN cl_corredor c ON c.id_corredor = p.id_corredor
                  LEFT JOIN cl_estoque e ON e.id_produto = p.id_produto
                """;
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Produto> produtos = new ArrayList<>();
            while (rs.next()) {
                produtos.add(mapearProduto(rs));
            }
            return produtos;
        } catch (SQLException e) {
            throw new DadosIndisponiveisException("listar produtos", e);
        }
    }

    /** Returns pairs [productId, similarId]. */
    public List<long[]> listarSimilares() {
        String sql = "SELECT id_produto, id_similar FROM cl_produto_similar";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<long[]> pares = new ArrayList<>();
            while (rs.next()) {
                pares.add(new long[]{rs.getLong(1), rs.getLong(2)});
            }
            return pares;
        } catch (SQLException e) {
            throw new DadosIndisponiveisException("listar similares", e);
        }
    }

    public List<Complemento> listarComplementos() {
        String sql = "SELECT id_produto, id_complementar, ds_motivo FROM cl_produto_complementar";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Complemento> complementos = new ArrayList<>();
            while (rs.next()) {
                complementos.add(new Complemento(rs.getLong(1), rs.getLong(2), rs.getString(3)));
            }
            return complementos;
        } catch (SQLException e) {
            throw new DadosIndisponiveisException("listar complementos", e);
        }
    }

    /** Moves every unit the system shows onto the shelf; returns the new shelf quantity. */
    public int reporGondola(long idProduto) {
        String sql = "UPDATE cl_estoque SET qt_gondola = qt_sistema, dt_atualizacao = SYSTIMESTAMP WHERE id_produto = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idProduto);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DadosIndisponiveisException("atualizar estoque", e);
        }
        String consulta = "SELECT qt_gondola FROM cl_estoque WHERE id_produto = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(consulta)) {
            ps.setLong(1, idProduto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DadosIndisponiveisException("consultar estoque", e);
        }
    }

    public void zerarGondola(long idProduto) {
        String sql = "UPDATE cl_estoque SET qt_gondola = 0, dt_atualizacao = SYSTIMESTAMP WHERE id_produto = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idProduto);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DadosIndisponiveisException("atualizar estoque", e);
        }
    }

    private Produto mapearProduto(ResultSet rs) throws SQLException {
        TipoDisponibilidade disponibilidade = TipoDisponibilidade.valueOf(rs.getString("tp_disponibilidade"));
        TipoAtendimento atendimento = TipoAtendimento.valueOf(rs.getString("tp_atendimento"));
        boolean marcaPropria = "S".equals(rs.getString("fl_marca_propria"));
        if (disponibilidade == TipoDisponibilidade.MOSTRUARIO) {
            return new ProdutoMostruario(rs.getLong("id_produto"), rs.getString("cd_sku"), rs.getString("nm_produto"),
                    rs.getString("ds_produto"), rs.getString("nm_marca"), marcaPropria, rs.getBigDecimal("vl_preco"),
                    rs.getString("ds_unidade"), rs.getInt("nr_corredor"), rs.getInt("nr_prateleira"), atendimento,
                    rs.getString("ds_palavras_chave"), rs.getInt("qt_sistema"), rs.getInt("qt_gondola"));
        }
        return new ProdutoProntaEntrega(rs.getLong("id_produto"), rs.getString("cd_sku"), rs.getString("nm_produto"),
                rs.getString("ds_produto"), rs.getString("nm_marca"), marcaPropria, rs.getBigDecimal("vl_preco"),
                rs.getString("ds_unidade"), rs.getInt("nr_corredor"), rs.getInt("nr_prateleira"), atendimento,
                rs.getString("ds_palavras_chave"), rs.getInt("qt_sistema"), rs.getInt("qt_gondola"));
    }
}
