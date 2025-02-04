package ad.t4_1.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.sql.DataSource;

import ad.t4_1.models.ZonaEnvio;
import ad.t4_1.db.SQLiteConnectionPool;

public class ZonaEnvioDAO implements Crud<ZonaEnvio> {
    private final DataSource dataSource;

    public ZonaEnvioDAO() {
        this.dataSource = SQLiteConnectionPool.getInstance().getDataSource();
    }

    private static ZonaEnvio resultToZonaEnvio(ResultSet rs) throws SQLException {
        return new ZonaEnvio(
            rs.getInt("id_zona"),
            rs.getString("nombre_zona"),
            rs.getDouble("tarifa_envio")
        );
    }

    @Override
    public Stream<ZonaEnvio> get() {
        final String sql = "SELECT * FROM Zonas_Envio";
        try {
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            return Stream.generate(() -> {
                try {
                    return rs.next() ? resultToZonaEnvio(rs) : null;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }).takeWhile(z -> z != null)
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
    public Optional<ZonaEnvio> get(int id) {
        final String sql = "SELECT * FROM Zonas_Envio WHERE id_zona = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            return Optional.of(resultToZonaEnvio(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insert(ZonaEnvio zona) {
        final String sql = "INSERT INTO Zonas_Envio (nombre_zona, tarifa_envio) VALUES (?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, zona.getNombre());
            pstmt.setDouble(2, zona.getTarifa());
            
            pstmt.executeUpdate();
            
            // Obtener el ID generado
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                zona.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(int id) {
        final String sql = "DELETE FROM Zonas_Envio WHERE id_zona = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean update(ZonaEnvio zona) {
        final String sql = "UPDATE Zonas_Envio SET nombre_zona = ?, tarifa_envio = ? WHERE id_zona = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, zona.getNombre());
            pstmt.setDouble(2, zona.getTarifa());
            pstmt.setInt(3, zona.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean update(int oldId, int newId) {
        final String sql = "UPDATE Zonas_Envio SET id_zona = ? WHERE id_zona = ?";
        
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
     * Obtiene todas las zonas con su número de clientes
     * @return Stream de arrays con [id_zona, nombre_zona, tarifa_envio, num_clientes]
     */
    public Stream<ZonaEnvio> getZonasConNumeroClientes() {
        final String sql = """
            SELECT z.*, COUNT(c.id_cliente) as num_clientes 
            FROM Zonas_Envio z 
            LEFT JOIN Clientes c ON z.id_zona = c.id_zona 
            GROUP BY z.id_zona
            """;
        
        try {
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            List<ZonaEnvio> listaZonas=new ArrayList<ZonaEnvio>();
            while(!rs.next()){
                listaZonas.add(new ZonaEnvio(
                    rs.getInt("id_zona"),
                    rs.getString("nombre_zona"),
                    rs.getDouble("tarifa_envio")));
            }
            return listaZonas.stream();
            // return Stream.generate(() -> {
            //     try {
            //         if (!rs.next()) return null;
            //         return new Object[] {
            //             rs.getInt("id_zona"),
            //             rs.getString("nombre_zona"),
            //             rs.getDouble("tarifa_envio"),
            //             rs.getInt("num_clientes")
            //         };
            //     } catch (SQLException e) {
            //         throw new RuntimeException(e);
            //     }
            // }).takeWhile(arr -> arr != null)
            //   .onClose(() -> {
            //     try {
            //         rs.close();
            //         stmt.close();
            //         conn.close();
            //     } catch (SQLException e) {
            //         throw new RuntimeException(e);
            //     }
            // });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
