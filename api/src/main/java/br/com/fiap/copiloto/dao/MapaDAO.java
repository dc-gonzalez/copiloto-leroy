package br.com.fiap.copiloto.dao;

import br.com.fiap.copiloto.exception.DadosIndisponiveisException;
import br.com.fiap.copiloto.infra.ConnectionFactory;
import br.com.fiap.copiloto.model.ArestaMapa;
import br.com.fiap.copiloto.model.NoMapa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MapaDAO {

    public List<NoMapa> listarNos() {
        String sql = "SELECT id_no, cd_no, tp_no, nr_x, nr_y FROM cl_no_mapa ORDER BY id_no";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<NoMapa> nos = new ArrayList<>();
            while (rs.next()) {
                nos.add(new NoMapa(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getInt(5)));
            }
            return nos;
        } catch (SQLException e) {
            throw new DadosIndisponiveisException("listar nós do mapa", e);
        }
    }

    public List<ArestaMapa> listarArestas() {
        String sql = "SELECT id_no_origem, id_no_destino, nr_distancia_m FROM cl_aresta_mapa";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<ArestaMapa> arestas = new ArrayList<>();
            while (rs.next()) {
                arestas.add(new ArestaMapa(rs.getLong(1), rs.getLong(2), rs.getDouble(3)));
            }
            return arestas;
        } catch (SQLException e) {
            throw new DadosIndisponiveisException("listar arestas do mapa", e);
        }
    }
}
