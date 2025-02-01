package ad.t4_1.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Pool de conexiones implementado como un Singleton.
 * Utiliza HikariCP para la gestión del pool.
 */
public class SQLiteConnectionPool {
    private static final Logger LOGGER = Logger.getLogger(SQLiteConnectionPool.class.getName());
    private static volatile SQLiteConnectionPool instance;
    private final HikariDataSource dataSource;
    private final SQLiteConfig properties;

    /**
     * Constructor privado que inicializa el pool de conexiones.
     * @param dbPath Ruta a la base de datos
     */
    private SQLiteConnectionPool(String dbPath) {
        this.properties = new SQLiteConfig(dbPath,null);
        this.dataSource = initPool();
    }

    /**
     * Constructor privado que inicializa el pool de conexiones con script personalizado.
     * @param dbPath Ruta a la base de datos
     * @param initScript Script SQL de inicialización
     */
    private SQLiteConnectionPool(String dbPath, Optional<String> initScript) {
        this.properties = new SQLiteConfig(dbPath, initScript);
        this.dataSource = initPool();
    }

    /**
     * Obtiene la instancia única del pool de conexiones.
     * Inicializa con la configuración por defecto si no existe.
     * @return La instancia del pool
     */
    public static SQLiteConnectionPool getInstance() {
        return getInstance("pedidos.db");
    }

    /**
     * Obtiene la instancia única del pool de conexiones con una base de datos específica.
     * @param dbPath Ruta a la base de datos
     * @return La instancia del pool
     */
    public static synchronized SQLiteConnectionPool getInstance(String dbPath) {
        if (instance == null) {
            synchronized (SQLiteConnectionPool.class) {
                if (instance == null) {
                    instance = new SQLiteConnectionPool(dbPath);
                }
            }
        }
        return instance;
    }

    /**
     * Obtiene la instancia única del pool de conexiones con una base de datos
     * y script de inicialización específicos.
     * @param dbPath Ruta a la base de datos
     * @param initScript Script SQL de inicialización
     * @return La instancia del pool
     */
    public static synchronized SQLiteConnectionPool getInstance(String dbPath, Optional<String> initScript) {
        if (instance == null) {
            synchronized (SQLiteConnectionPool.class) {
                if (instance == null) {
                    instance = new SQLiteConnectionPool(dbPath, initScript);
                }
            }
        }
        return instance;
    }

    /**
     * Inicializa el pool de conexiones con HikariCP.
     * @return DataSource configurado
     */
    private HikariDataSource initPool() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(properties.getJdbcUrl());
            config.setDriverClassName(properties.getDriverClass());
            config.setMinimumIdle(properties.getMinPoolSize());
            config.setMaximumPoolSize(properties.getMaxPoolSize());
            config.setConnectionTimeout(SQLiteConfig.CONNECTION_TIMEOUT);
            config.setAutoCommit(true);

            return new HikariDataSource(config);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing connection pool", e);
            throw new RuntimeException("Could not initialize connection pool", e);
        }
    }

    /**
     * Obtiene una conexión del pool.
     * @return Una conexión a la base de datos
     * @throws SQLException si hay un error al obtener la conexión
     */
    public Connection getConnection() throws SQLException {
        try {
            Connection conn = dataSource.getConnection();
            if (conn == null) {
                throw new SQLException("Could not get connection from pool");
            }
            return conn;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting connection from pool", e);
            throw e;
        }
    }

    /**
     * Cierra el pool de conexiones y libera los recursos.
     */
    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            try {
                dataSource.close();
                instance = null;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing connection pool", e);
            }
        }
    }

    /**
     * Verifica si el pool está cerrado.
     * @return true si el pool está cerrado
     */
    public boolean isClosed() {
        return dataSource == null || dataSource.isClosed();
    }

    /**
     * Obtiene el número actual de conexiones activas.
     * @return número de conexiones activas
     */
    public int getActiveConnections() {
        return dataSource.getHikariPoolMXBean().getActiveConnections();
    }

    /**
     * Obtiene el número total de conexiones en el pool.
     * @return número total de conexiones
     */
    public int getTotalConnections() {
        return dataSource.getHikariPoolMXBean().getTotalConnections();
    }

    /**
     * Evita la clonación del singleton.
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("ConnectionPool cannot be cloned");
    }
}