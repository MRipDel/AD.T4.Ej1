package ad.t4_1.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.stream.Stream;

import javax.sql.DataSource;

import ad.t4_1.models.Cliente;
import ad.t4_1.db.SQLiteConnectionPool;

public class ClienteDAO implements Crud<Cliente> {
    private final DataSource dataSource;

    public ClienteDAO() {
        this.dataSource = SQLiteConnectionPool.getInstance().getDataSource();
    }

    private static Cliente resultToCliente(ResultSet rs) throws SQLException {
        return new Cliente(
            rs.getInt("id_cliente"),
            rs.getString("nombre"),
            rs.getString("email"),
            rs.getString("telefono"),
            rs.getInt("id_zona")
        );
    }

    @Override
    public Stream<Cliente> get() {
        final String sql = "SELECT * FROM Clientes";
        try {
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            return Stream.generate(() -> {
                try {
                    return rs.next() ? resultToCliente(rs) : null;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).takeWhile(c -> c != null)
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
    public Optional<Cliente> get(int id) {
        final String sql = "SELECT * FROM Clientes WHERE id_cliente = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            return rs.next() ? Optional.of(resultToCliente(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insert(Cliente cliente) {
        final String sql = "INSERT INTO Clientes (nombre, email, telefono, id_zona) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, cliente.getNombre());
            pstmt.setString(2, cliente.getEmail());
            pstmt.setString(3, cliente.getTelefono());
            pstmt.setInt(4, cliente.getIdZona());
            
            pstmt.executeUpdate();
            
            // Obtener el ID generado
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                cliente.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(int id) {
        final String sql = "DELETE FROM Clientes WHERE id_cliente = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean update(Cliente cliente) {
        final String sql = "UPDATE Clientes SET nombre = ?, email = ?, telefono = ?, id_zona = ? WHERE id_cliente = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cliente.getNombre());
            pstmt.setString(2, cliente.getEmail());
            pstmt.setString(3, cliente.getTelefono());
            pstmt.setInt(4, cliente.getIdZona());
            pstmt.setInt(5, cliente.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean update(int oldId, int newId) {
        final String sql = "UPDATE Clientes SET id_cliente = ? WHERE id_cliente = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, newId);
            pstmt.setInt(2, oldId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}