package br.com.fiap.copiloto.dao;

import br.com.fiap.copiloto.exception.DadosIndisponiveisException;
import br.com.fiap.copiloto.infra.ConnectionFactory;
import br.com.fiap.copiloto.model.Chamado;
import br.com.fiap.copiloto.model.StatusChamado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChamadoDAO {

    private static final String COLUNAS = """
            id_chamado, nr_senha, id_projeto, id_corredor, id_produto, ds_motivo, st_chamado,
            nm_vendedor, dt_abertura, dt_aceite, dt_conclusao, CAST(SYSTIMESTAMP AS TIMESTAMP) AS dt_agora
            """;

    public long inserir(Long idProjeto, int idCorredor, Long idProduto, String motivo) {
        String sql = """
                INSERT INTO cl_chamado (nr_senha, id_projeto, id_corredor, id_produto, ds_motivo)
                VALUES (seq_cl_senha_chamado.NEXTVAL, ?, ?, ?, ?)
                """;
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"ID_CHAMADO"})) {
            if (idProjeto == null) {
                ps.setNull(1, Types.NUMERIC);
            } else {
                ps.setLong(1, idProjeto);
            }
            ps.setInt(2, idCorredor);
            if (idProduto == null) {
                ps.setNull(3, Types.NUMERIC);
            } else {
                ps.setLong(3, idProduto);
            }
            ps.setString(4, motivo);
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                return chaves.getLong(1);
            }
        } catch (SQLException e) {
            throw new DadosIndisponiveisException("abrir chamado", e);
        }
    }

    public Optional<Chamado> buscar(long id) {
        String sql = "SELECT " + COLUNAS + " FROM cl_chamado WHERE id_chamado = ?";
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DadosIndisponiveisException("buscar chamado", e);
        }
    }

    /** Active tickets in FIFO order (oldest first), plus those closed in the last hour. */
    public List<Chamado> listarFila() {
        String sql = "SELECT " + COLUNAS + """
                  FROM cl_chamado
                 WHERE st_chamado IN ('ABERTO', 'A_CAMINHO')
                    OR dt_conclusao > CAST(SYSTIMESTAMP AS TIMESTAMP) - INTERVAL '1' HOUR
                 ORDER BY dt_abertura
                """;
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Chamado> chamados = new ArrayList<>();
            while (rs.next()) {
                chamados.add(mapear(rs));
            }
            return chamados;
        } catch (SQLException e) {
            throw new DadosIndisponiveisException("listar fila de chamados", e);
        }
    }

    /** Number of open tickets created before the given one (its position in the queue). */
    public int contarNaFrente(long idChamado) {
        String sql = """
                SELECT COUNT(*)
                  FROM cl_chamado
                 WHERE st_chamado = 'ABERTO'
                   AND dt_abertura < (SELECT dt_abertura FROM cl_chamado WHERE id_chamado = ?)
                """;
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, idChamado);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DadosIndisponiveisException("calcular posição na fila", e);
        }
    }

    public void aceitar(long id, String vendedor) {
        String sql = """
                UPDATE cl_chamado
                   SET st_chamado = 'A_CAMINHO', nm_vendedor = ?, dt_aceite = SYSTIMESTAMP
                 WHERE id_chamado = ?
                """;
        executarAtualizacao(sql, "aceitar chamado", ps -> {
            ps.setString(1, vendedor);
            ps.setLong(2, id);
        });
    }

    public void encerrar(long id, StatusChamado status) {
        String sql = "UPDATE cl_chamado SET st_chamado = ?, dt_conclusao = SYSTIMESTAMP WHERE id_chamado = ?";
        executarAtualizacao(sql, "encerrar chamado", ps -> {
            ps.setString(1, status.name());
            ps.setLong(2, id);
        });
    }

    private interface Parametros {
        void aplicar(PreparedStatement ps) throws SQLException;
    }

    private void executarAtualizacao(String sql, String operacao, Parametros parametros) {
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            parametros.aplicar(ps);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DadosIndisponiveisException(operacao, e);
        }
    }

    private Chamado mapear(ResultSet rs) throws SQLException {
        long idProjeto = rs.getLong("id_projeto");
        Long projeto = rs.wasNull() ? null : idProjeto;
        long idProduto = rs.getLong("id_produto");
        Long produto = rs.wasNull() ? null : idProduto;
        return new Chamado(
                rs.getLong("id_chamado"),
                rs.getInt("nr_senha"),
                projeto,
                rs.getInt("id_corredor"),
                produto,
                rs.getString("ds_motivo"),
                StatusChamado.valueOf(rs.getString("st_chamado")),
                rs.getString("nm_vendedor"),
                paraData(rs.getTimestamp("dt_abertura")),
                paraData(rs.getTimestamp("dt_aceite")),
                paraData(rs.getTimestamp("dt_conclusao")),
                paraData(rs.getTimestamp("dt_agora")));
    }

    private static LocalDateTime paraData(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
