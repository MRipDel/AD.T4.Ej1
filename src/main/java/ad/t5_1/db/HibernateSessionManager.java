package ad.t5_1.db;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestor de sesiones de Hibernate que implementa una interfaz compatible con SQLiteConnectionPool.
 * Esta clase es un adaptador entre la API de Hibernate y la interfaz usada en la aplicación original.
 */
public class HibernateSessionManager {
    
    /** Logger para registrar eventos y errores */
    private static final Logger LOGGER = Logger.getLogger(HibernateSessionManager.class.getName());
    
    /** Instancia única del gestor de sesiones */
    private static volatile HibernateSessionManager instance;
    
    /** Factory de sesiones de Hibernate */
    private final SessionFactory sessionFactory;
    
    /** DataSource adaptado para la interfaz JDBC */
    private final HibernateDataSourceAdapter dataSource;
    
    /** Tiempo máximo de espera para una conexión (30 segundos) */
    public static final int CONNECTION_TIMEOUT = 30000;

    /**
     * Constructor privado para la implementación del patrón Singleton.
     * Inicializa el SessionFactory con la configuración básica.
     */
    private HibernateSessionManager() {
        try {
            Configuration config = new Configuration().configure("hibernate.cfg.xml");
            this.sessionFactory = config.buildSessionFactory();
            this.dataSource = new HibernateDataSourceAdapter(sessionFactory);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing Hibernate SessionFactory", e);
            throw new RuntimeException("Could not initialize Hibernate SessionFactory", e);
        }
    }

    /**
     * Constructor privado con soporte para configuración personalizada.
     * 
     * @param configFile Ruta al archivo de configuración de Hibernate
     */
    private HibernateSessionManager(String configFile) {
        try {
            Configuration config = new Configuration().configure(configFile);
            this.sessionFactory = config.buildSessionFactory();
            this.dataSource = new HibernateDataSourceAdapter(sessionFactory);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing Hibernate SessionFactory", e);
            throw new RuntimeException("Could not initialize Hibernate SessionFactory", e);
        }
    }
    
    /**
     * Obtiene la instancia única del gestor de sesiones.
     * Si no existe, la crea con la configuración por defecto.
     * 
     * @return Instancia del gestor de sesiones
     */
    public static HibernateSessionManager getInstance() {
        if (instance == null) {
            synchronized (HibernateSessionManager.class) {
                if (instance == null) {
                    instance = new HibernateSessionManager();
                }
            }
        }
        return instance;
    }

    /**
     * Obtiene la instancia única del gestor especificando un archivo de configuración.
     * 
     * @param configFile Ruta al archivo de configuración de Hibernate
     * @return Instancia del gestor de sesiones
     */
    public static synchronized HibernateSessionManager getInstance(String configFile) {
        if (instance == null) {
            synchronized (HibernateSessionManager.class) {
                if (instance == null) {
                    instance = new HibernateSessionManager(configFile);
                }
            }
        }
        return instance;
    }

    /**
     * Obtiene la instancia única especificando la base de datos y script de inicialización.
     * 
     * @param dbPath Ruta a la base de datos
     * @param initScript Script SQL de inicialización
     * @return Instancia del gestor de sesiones
     */
    public static synchronized HibernateSessionManager getInstance(String dbPath, Optional<String> initScript) {
        if (instance == null) {
            synchronized (HibernateSessionManager.class) {
                if (instance == null) {
                    // Establecer la URL de conexión
                    System.setProperty("hibernate.connection.url", "jdbc:sqlite:" + dbPath);
                    
                    // La inicialización del esquema se maneja en hibernate.cfg.xml
                    // con la propiedad hibernate.hbm2ddl.auto
                    
                    instance = new HibernateSessionManager();
                }
            }
        }
        return instance;
    }

    /**
     * Obtiene el SessionFactory de Hibernate.
     * 
     * @return SessionFactory configurado
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Obtiene una sesión de Hibernate.
     * 
     * @return Sesión de Hibernate
     */
    public Session openSession() {
        return sessionFactory.openSession();
    }

    /**
     * Obtiene la fuente de datos adaptada para JDBC.
     * 
     * @return DataSource adaptado
     */
    public DataSource getDataSource() {
        return this.dataSource;
    }

    /**
     * Cierra el SessionFactory y libera los recursos.
     * También reinicia la instancia singleton.
     */
    public void closePool() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            try {
                sessionFactory.close();
                instance = null;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing SessionFactory", e);
            }
        }
    }

    /**
     * Verifica si el SessionFactory está cerrado.
     * 
     * @return true si el SessionFactory está cerrado, false en caso contrario
     */
    public boolean isClosed() {
        return sessionFactory == null || sessionFactory.isClosed();
    }

    /**
     * Obtiene el número de conexiones activas.
     * Método de compatibilidad.
     * 
     * @return 0, ya que Hibernate gestiona el pool internamente
     */
    public int getActiveConnections() {
        // Este método es para compatibilidad
        return 0;
    }

    /**
     * Obtiene el número total de conexiones.
     * Método de compatibilidad.
     * 
     * @return 0, ya que Hibernate gestiona el pool internamente
     */
    public int getTotalConnections() {
        // Este método es para compatibilidad
        return 0;
    }

    /**
     * Evita la clonación del singleton.
     * 
     * @throws CloneNotSupportedException siempre, para prevenir la clonación
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("HibernateSessionManager cannot be cloned");
    }

    /**
     * Clase interna que adapta SessionFactory a DataSource.
     * Permite el uso de SessionFactory a través de la interfaz JDBC DataSource.
     */
    private static class HibernateDataSourceAdapter implements DataSource {
        private final SessionFactory sessionFactory;
        
        public HibernateDataSourceAdapter(SessionFactory sessionFactory) {
            this.sessionFactory = sessionFactory;
        }
        
        @Override
        public Connection getConnection() throws SQLException {
            try {
                Session session = sessionFactory.openSession();
                return session.doReturningWork(connection -> {
                    // Desactivar auto-commit para permitir transacciones explícitas
                    connection.setAutoCommit(false);
                    return connection;
                });
            } catch (Exception e) {
                throw new SQLException("Error obtaining connection from Hibernate", e);
            }
        }
        
        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            // Ignoramos los credenciales, ya que están en la configuración
            return getConnection();
        }
        
        // Métodos no implementados que no se utilizan en nuestra aplicación
        @Override
        public java.io.PrintWriter getLogWriter() throws SQLException {
            throw new UnsupportedOperationException("Method not implemented");
        }
        
        @Override
        public void setLogWriter(java.io.PrintWriter out) throws SQLException {
            throw new UnsupportedOperationException("Method not implemented");
        }
        
        @Override
        public void setLoginTimeout(int seconds) throws SQLException {
            // No hacemos nada, ya que el timeout se configura en Hibernate
        }
        
        @Override
        public int getLoginTimeout() throws SQLException {
            return CONNECTION_TIMEOUT / 1000; // Convertir milisegundos a segundos
        }
        
        @Override
        public java.util.logging.Logger getParentLogger() {
            return Logger.getLogger("global");
        }
        
        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            if (iface.isInstance(this)) {
                return iface.cast(this);
            }
            throw new SQLException("DataSource cannot be unwrapped");
        }
        
        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return iface.isInstance(this);
        }
    }
}