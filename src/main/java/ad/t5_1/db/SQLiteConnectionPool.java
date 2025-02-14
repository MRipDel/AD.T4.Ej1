package ad.t5_1.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * Implementa un pool de conexiones para SQLite utilizando el patrón Singleton.
 * Esta clase gestiona un conjunto de conexiones reutilizables a la base de datos 
 * usando HikariCP como implementación del pool.
 */
public class SQLiteConnectionPool {
    
    /** Logger para registrar eventos y errores */
    private static final Logger LOGGER = Logger.getLogger(SQLiteConnectionPool.class.getName());
    
    /** Instancia única del pool de conexiones */
    private static volatile SQLiteConnectionPool instance;
    
    /** Fuente de datos que gestiona el pool de conexiones */
    private final HikariDataSource dataSource;
    
    /** Configuración de la base de datos SQLite */
    private final SQLiteConfig properties;

    /**
     * Constructor privado para la implementación del patrón Singleton.
     * Inicializa el pool de conexiones con la configuración básica.
     * 
     * @param dbPath Ruta al archivo de la base de datos
     */
    private SQLiteConnectionPool(String dbPath) {
        this.properties = new SQLiteConfig(dbPath, Optional.empty());
        this.dataSource = initPool();
    }

    /**
     * Constructor privado con soporte para script de inicialización.
     * 
     * @param dbPath Ruta al archivo de la base de datos
     * @param initScript Script SQL opcional para inicializar la base de datos
     */
    private SQLiteConnectionPool(String dbPath, Optional<String> initScript) {
        this.properties = new SQLiteConfig(dbPath, initScript);
        this.dataSource = initPool();
    }

    /**
     * Obtiene la instancia única del pool de conexiones.
     * Si no existe, la crea con la configuración por defecto.
     * 
     * @return Instancia del pool de conexiones
     */
    public static SQLiteConnectionPool getInstance() {
        return getInstance("pedidos.db");
    }

    /**
     * Obtiene la instancia única del pool especificando la base de datos.
     * 
     * @param dbPath Ruta a la base de datos
     * @return Instancia del pool de conexiones
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
     * Obtiene la instancia única del pool especificando la base de datos y script de inicialización.
     * 
     * @param dbPath Ruta a la base de datos
     * @param initScript Script SQL de inicialización
     * @return Instancia del pool de conexiones
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
     * Configura los parámetros básicos como URL, driver, tamaño del pool, etc.
     * 
     * @return DataSource configurado
     * @throws RuntimeException si hay errores en la inicialización
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
     * 
     * @return Conexión a la base de datos
     * @throws SQLException si no se puede obtener una conexión
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
     * Obtiene la fuente de datos subyacente.
     * 
     * @return DataSource que gestiona el pool de conexiones
     */
    public DataSource getDataSource() {
        return this.dataSource;
    }

    /**
     * Cierra el pool de conexiones y libera los recursos.
     * También reinicia la instancia singleton.
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
     * 
     * @return true si el pool está cerrado, false en caso contrario
     */
    public boolean isClosed() {
        return dataSource == null || dataSource.isClosed();
    }

    /**
     * Obtiene el número de conexiones activas en el pool.
     * 
     * @return número de conexiones activas
     */
    public int getActiveConnections() {
        return dataSource.getHikariPoolMXBean().getActiveConnections();
    }

    /**
     * Obtiene el número total de conexiones en el pool.
     * 
     * @return número total de conexiones
     */
    public int getTotalConnections() {
        return dataSource.getHikariPoolMXBean().getTotalConnections();
    }

    /**
     * Evita la clonación del singleton.
     * 
     * @throws CloneNotSupportedException siempre, para prevenir la clonación
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("ConnectionPool cannot be cloned");
    }
}