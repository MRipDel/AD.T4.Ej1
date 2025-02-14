package ad.t5_1.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.util.Optional;
import java.util.stream.Stream;

import javax.sql.DataSource;

import ad.t5_1.db.SQLiteConnectionPool;
import ad.t5_1.models.Pedido;

/**
 * Implementación del acceso a datos para la entidad Pedido.
 * Proporciona métodos para realizar operaciones CRUD básicas sobre la tabla Pedidos,
 * así como consultas específicas relacionadas con la gestión de pedidos por cliente
 * y cálculos de importes totales.
 */
public class PedidoDAO implements Crud<Pedido> {
    
    /** Fuente de datos para la conexión a la base de datos */
    private final DataSource dataSource;

    /**
     * Constructor por defecto.
     * Inicializa la fuente de datos obteniendo una instancia del pool de conexiones.
     */
    public PedidoDAO() {
        this.dataSource = SQLiteConnectionPool.getInstance().getDataSource();
    }

    /**
     * Convierte un registro de la base de datos en un objeto Pedido.
     * @param rs ResultSet con los datos del pedido
     * @return Objeto Pedido con los datos del registro
     * @throws SQLException si ocurre un error al acceder a los datos
     */
    private static Pedido resultToPedido(ResultSet rs) throws SQLException {
        return new Pedido(
            rs.getInt("id_pedido"),
            rs.getDate("fecha").toLocalDate(),
            rs.getDouble("importe_total"),
            rs.getInt("id_cliente")
        );
    }

    /**
     * Recupera todos los pedidos almacenados en la base de datos.
     * @return Stream de objetos Pedido
     * @throws RuntimeException si ocurre un error en el acceso a la base de datos
     */
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

    /**
     * Recupera un pedido específico por su identificador.
     * @param id Identificador del pedido a buscar
     * @return Optional con el pedido si existe, Optional vacío si no
     * @throws RuntimeException si ocurre un error en el acceso a la base de datos
     */
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

    /**
     * Inserta un nuevo pedido en la base de datos.
     * @param pedido Pedido a insertar
     * @throws RuntimeException si ocurre un error en el acceso a la base de datos
     */
    @Override
    public void insert(Pedido pedido) {
        final String sql = "INSERT INTO Pedidos (fecha, importe_total, id_cliente) VALUES (?, ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setDate(1, Date.valueOf(pedido.getFecha()));
            pstmt.setDouble(2, pedido.getImporteTotal());
            pstmt.setInt(3, pedido.getIdCliente());
            
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                pedido.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Elimina un pedido de la base de datos.
     * @param id Identificador del pedido a eliminar
     * @return true si se eliminó el pedido, false si no existía
     * @throws RuntimeException si ocurre un error en el acceso a la base de datos
     */
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

    /**
     * Actualiza los datos de un pedido existente.
     * @param pedido Pedido con los datos actualizados
     * @return true si se actualizó el pedido, false si no existía
     * @throws RuntimeException si ocurre un error en el acceso a la base de datos
     */
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

    /**
     * Actualiza el identificador de un pedido.
     * @param oldId Identificador actual del pedido
     * @param newId Nuevo identificador para el pedido
     * @return true si se actualizó el identificador, false si no existía el pedido
     * @throws RuntimeException si ocurre un error en el acceso a la base de datos
     */
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

    /**
     * Recupera todos los pedidos de un cliente específico.
     * Los pedidos se ordenan por fecha en orden descendente.
     * @param idCliente Identificador del cliente
     * @return Stream de pedidos del cliente
     * @throws RuntimeException si ocurre un error en el acceso a la base de datos
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
     * Calcula el importe total de todos los pedidos de un cliente.
     * @param idCliente Identificador del cliente
     * @return Total gastado por el cliente
     * @throws RuntimeException si ocurre un error en el acceso a la base de datos
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