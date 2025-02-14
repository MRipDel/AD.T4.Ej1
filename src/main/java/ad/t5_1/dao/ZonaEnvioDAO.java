package ad.t5_1.dao;

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

import ad.t5_1.db.SQLiteConnectionPool;
import ad.t5_1.models.ZonaEnvio;

/**
 * Implementación del acceso a datos para la entidad ZonaEnvio.
 * Proporciona métodos para realizar operaciones CRUD básicas sobre la tabla Zonas_Envio,
 * así como consultas específicas relacionadas con la gestión de zonas de envío y sus estadísticas.
 */
public class ZonaEnvioDAO implements Crud<ZonaEnvio> {
    
    /** Fuente de datos para la conexión a la base de datos */
    private final DataSource dataSource;

    /**
     * Constructor por defecto.
     * Inicializa la fuente de datos obteniendo una instancia del pool de conexiones.
     */
    public ZonaEnvioDAO() {
        this.dataSource = SQLiteConnectionPool.getInstance().getDataSource();
    }

    /**
     * Convierte un registro de la base de datos en un objeto ZonaEnvio.
     * @param rs ResultSet con los datos de la zona de envío
     * @return Objeto ZonaEnvio con los datos del registro
     * @throws SQLException si ocurre un error al acceder a los datos
     */
    private static ZonaEnvio resultToZonaEnvio(ResultSet rs) throws SQLException {
        return new ZonaEnvio(
            rs.getInt("id_zona"),
            rs.getString("nombre_zona"),
            rs.getDouble("tarifa_envio")
        );
    }

    /**
     * Recupera todas las zonas de envío almacenadas en la base de datos.
     * @return Stream de objetos ZonaEnvio
     * @throws RuntimeException si ocurre un error en el acceso a la base de datos
     */
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

    /**
     * Recupera una zona de envío específica por su identificador.
     * @param id Identificador de la zona de envío a buscar
     * @return Optional con la zona de envío si existe, Optional vacío si no
     * @throws RuntimeException si ocurre un error en el acceso a la base de datos
     */
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

    /**
     * Inserta una nueva zona de envío en la base de datos.
     * @param zona ZonaEnvio a insertar
     * @throws RuntimeException si ocurre un error en el acceso a la base de datos
     */
    @Override
    public void insert(ZonaEnvio zona) {
        final String sql = "INSERT INTO Zonas_Envio (nombre_zona, tarifa_envio) VALUES (?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, zona.getNombre());
            pstmt.setDouble(2, zona.getTarifa());
            
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                zona.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Elimina una zona de envío de la base de datos.
     * @param id Identificador de la zona de envío a eliminar
     * @return true si se eliminó la zona, false si no existía
     * @throws RuntimeException si ocurre un error en el acceso a la base de datos
     */
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

    /**
     * Actualiza los datos de una zona de envío existente.
     * @param zona ZonaEnvio con los datos actualizados
     * @return true si se actualizó la zona, false si no existía
     * @throws RuntimeException si ocurre un error en el acceso a la base de datos
     */
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

    /**
     * Actualiza el identificador de una zona de envío.
     * @param oldId Identificador actual de la zona de envío
     * @param newId Nuevo identificador para la zona de envío
     * @return true si se actualizó el identificador, false si no existía la zona
     * @throws RuntimeException si ocurre un error en el acceso a la base de datos
     */
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
     * Obtiene todas las zonas de envío junto con el número de clientes en cada zona.
     * Realiza un LEFT JOIN con la tabla de clientes para contar el número de clientes
     * por zona, incluyendo zonas sin clientes.
     * 
     * @return Stream de objetos ZonaEnvio con información adicional sobre el número de clientes
     * @throws RuntimeException si ocurre un error en el acceso a la base de datos
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
            List<ZonaEnvio> listaZonas = new ArrayList<>();
            while (rs.next()) {
                listaZonas.add(new ZonaEnvio(
                    rs.getInt("id_zona"),
                    rs.getString("nombre_zona"),
                    rs.getDouble("tarifa_envio")));
            }
            return listaZonas.stream();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}