package ad.t4_1.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.util.Optional;
import java.util.stream.Stream;

import javax.sql.DataSource;

import ad.t4_1.models.Pedido;
import ad.t4_1.db.SQLiteConnectionPool;

public class PedidoDAO implements Crud<Pedido> {
    private final DataSource dataSource;

    public PedidoDAO() {
        this.dataSource = SQLiteConnectionPool.getInstance().getDataSource();
    }

    private static Pedido resultToPedido(ResultSet rs) throws SQLException {
        return new Pedido(
            rs.getInt("id_pedido"),
            rs.getDate("fecha").toLocalDate(),
            rs.getDouble("importe_total"),
            rs.getInt("id_cliente")
        );
    }

    @Override
    public Stream<Pedido> get() {
        final String sql = "SELECT * FROM Pedidos";
        try {
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            return Stream.generate(() -> {
                try {
                    return rs.next() ? resultToPedido(rs) : null;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).takeWhile(p -> p != null)
              .onClose(() -> {
                try {
                    rs.close();
                    stmt.close();
                    conn.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Pedido> get(int id) {
        final String sql = "SELECT * FROM Pedidos WHERE id_pedido = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            return rs.next() ? Optional.of(resultToPedido(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insert(Pedido pedido) {
        final String sql = "INSERT INTO Pedidos (fecha, importe_total, id_cliente) VALUES (?, ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setDate(1, Date.valueOf(pedido.getFecha()));
            pstmt.setDouble(2, pedido.getImporteTotal());
            pstmt.setInt(3, pedido.getIdCliente());
            
            pstmt.executeUpdate();
            
            // Obtener el ID generado
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                pedido.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(int id) {
        final String sql = "DELETE FROM Pedidos WHERE id_pedido = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean update(Pedido pedido) {
        final String sql = "UPDATE Pedidos SET fecha = ?, importe_total = ?, id_cliente = ? WHERE id_pedido = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, Date.valueOf(pedido.getFecha()));
            pstmt.setDouble(2, pedido.getImporteTotal());
            pstmt.setInt(3, pedido.getIdCliente());
            pstmt.setInt(4, pedido.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean update(int oldId, int newId) {
        final String sql = "UPDATE Pedidos SET id_pedido = ? WHERE id_pedido = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, newId);
            pstmt.setInt(2, oldId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Métodos específicos para Pedidos

    /**
     * Obtiene todos los pedidos de un cliente específico
     * @param idCliente ID del cliente
     * @return Stream de pedidos del cliente
     */
    public Stream<Pedido> getPedidosPorCliente(int idCliente) {
        final String sql = "SELECT * FROM Pedidos WHERE id_cliente = ? ORDER BY fecha DESC";
        
        try {
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, idCliente);
            ResultSet rs = pstmt.executeQuery();
            
            return Stream.generate(() -> {
                try {
                    return rs.next() ? resultToPedido(rs) : null;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).takeWhile(p -> p != null)
              .onClose(() -> {
                try {
                    rs.close();
                    pstmt.close();
                    conn.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Calcula el total gastado por un cliente específico
     * @param idCliente ID del cliente
     * @return Total gastado por el cliente
     */
    public double getTotalGastadoPorCliente(int idCliente) {
        final String sql = "SELECT SUM(importe_total) as total FROM Pedidos WHERE id_cliente = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idCliente);
            ResultSet rs = pstmt.executeQuery();
            
            return rs.next() ? rs.getDouble("total") : 0.0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}