package br.com.fiap.copiloto.dao;

import br.com.fiap.copiloto.exception.DadosIndisponiveisException;
import br.com.fiap.copiloto.infra.ConnectionFactory;
import br.com.fiap.copiloto.model.ItemProjeto;
import br.com.fiap.copiloto.model.Projeto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjetoDAO {

    /** Inserts the project and all its items in a single transaction. */
    public long inserir(String titulo, String problema, List<ItemProjeto> itens) {
        String sqlProjeto = "INSERT INTO cl_projeto (ds_titulo, ds_problema) VALUES (?, ?)";
        String sqlItem = """
                INSERT INTO cl_item_projeto (id_projeto, id_produto, qt_item, fl_essencial, ds_motivo)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection con = ConnectionFactory.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement psProjeto = con.prepareStatement(sqlProjeto, new String[]{"ID_PROJETO"});
                 PreparedStatement psItem = con.prepareStatement(sqlItem)) {
                psProjeto.setString(1, titulo);
                psProjeto.setString(2, problema);
                psProjeto.executeUpdate();
                long idProjeto;
                try (ResultSet chaves = psProjeto.getGeneratedKeys()) {
                    chaves.next();
                    idProjeto = chaves.getLong(1);
                }
                for (ItemProjeto item : itens) {
                    psItem.setLong(1, idProjeto);
                    psItem.setLong(2, item.idProduto());
                    psItem.setInt(3, item.quantidade());
                    psItem.setString(4, item.essencial() ? "S" : "N");
                    psItem.setString(5, item.motivo());
                    psItem.addBatch();
                }
                psItem.executeBatch();
                con.commit();
                return idProjeto;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DadosIndisponiveisException("salvar projeto", e);
        }
    }

    public Optional<Projeto> buscar(long id) {
        String sqlProjeto = "SELECT id_projeto, ds_titulo, ds_problema, dt_criacao FROM cl_projeto WHERE id_projeto = ?";
        String sqlItens = """
                SELECT id_produto, qt_item, fl_essencial, ds_motivo
                  FROM cl_item_projeto
                 WHERE id_projeto = ?
                """;
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement psProjeto = con.prepareStatement(sqlProjeto);
             PreparedStatement psItens = con.prepareStatement(sqlItens)) {
            psProjeto.setLong(1, id);
            try (ResultSet rs = psProjeto.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                psItens.setLong(1, id);
                List<ItemProjeto> itens = new ArrayList<>();
                try (ResultSet rsItens = psItens.executeQuery()) {
                    while (rsItens.next()) {
                        itens.add(new ItemProjeto(rsItens.getLong(1), rsItens.getInt(2),
                                "S".equals(rsItens.getString(3)), rsItens.getString(4)));
                    }
                }
                return Optional.of(new Projeto(rs.getLong(1), rs.getString(2), rs.getString(3),
                        rs.getTimestamp(4).toLocalDateTime(), itens));
            }
        } catch (SQLException e) {
            throw new DadosIndisponiveisException("buscar projeto", e);
        }
    }
}
