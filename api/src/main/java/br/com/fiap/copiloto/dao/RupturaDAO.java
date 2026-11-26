package br.com.fiap.copiloto.dao;

import br.com.fiap.copiloto.exception.DadosIndisponiveisException;
import br.com.fiap.copiloto.infra.ConnectionFactory;
import br.com.fiap.copiloto.model.DesfechoRuptura;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class RupturaDAO {

    public long inserir(long idProduto, Long idProjeto) {
        String sql = "INSERT INTO cl_ruptura (id_produto, id_projeto, tp_desfecho) VALUES (?, ?, 'PENDENTE')";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID_RUPTURA"})) {
            ps.setLong(1, idProduto);
            if (idProjeto == null) {
                ps.setNull(2, Types.NUMERIC);
            } else {
                ps.setLong(2, idProjeto);
            }
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return chaves.getLong(1);
            }
        } catch (SQLException e) {
            throw new DadosIndisponiveisException("registrar ruptura", e);
        }
    }

    public boolean atualizarDesfecho(long idRuptura, DesfechoRuptura desfecho) {
        String sql = "UPDATE cl_ruptura SET tp_desfecho = ? WHERE id_ruptura = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, desfecho.name());
            ps.setLong(2, idRuptura);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new DadosIndisponiveisException("atualizar ruptura", e);
        }
    }
}
