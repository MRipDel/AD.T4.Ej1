package ad.t5_1.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gestiona la configuración y la inicialización de la base de datos SQLite.
 * Proporciona funcionalidades para crear la base de datos, ejecutar scripts SQL iniciales
 * y gestionar las propiedades de conexión.
 */
public class SQLiteConfig {
    /** URL de conexión JDBC */
    private String jdbcUrl;
    
    /** Clase del driver JDBC */
    private String driverClass;
    
    /** Tamaño mínimo del pool de conexiones */
    private int minPoolSize;
    
    /** Tamaño máximo del pool de conexiones */
    private int maxPoolSize;
    
    /** Nombre de la base de datos */
    private String dbName;
    
    /** Script SQL de inicialización */
    private String initScript;
    
    /** Tiempo máximo de espera para una conexión (30 segundos) */
    public static final int CONNECTION_TIMEOUT = 30000;

    /**
     * Constructor de la configuración SQLite.
     * @param dbPath Ruta al archivo de la base de datos
     * @param initScriptPath Ruta opcional al script de inicialización
     */
    public SQLiteConfig(String dbPath, Optional<String> initScriptPath) {
        this.dbName = dbPath;
        this.initScript = initScriptPath.orElse(null);
        this.jdbcUrl = "jdbc:sqlite:" + dbPath;
        this.driverClass = "org.sqlite.JDBC";
        this.minPoolSize = 5;
        this.maxPoolSize = 10;
        
        initializeIfNeeded();
    }

    /**
     * Verifica si la base de datos necesita ser inicializada y la crea si es necesario.
     */
    private void initializeIfNeeded() {
        Path dbPath = Path.of(dbName);
        if (!Files.exists(dbPath)) {
            createDatabase();
        }
    }

    /**
     * Crea una nueva base de datos y ejecuta el script de inicialización si existe.
     * @throws RuntimeException si hay errores durante la creación
     */
    private void createDatabase() {
        try {
            try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
                executeSqlScript(conn);
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Error initializing database", e);
        }
    }

    /**
     * Divide un script SQL en sentencias individuales.
     * Maneja correctamente bloques BEGIN/END y casos especiales.
     * 
     * @param st InputStream con el contenido del script SQL
     * @return Lista de sentencias SQL
     * @throws IOException si hay errores al leer el script
     */
    private List<String> splitSQL(InputStream st) throws IOException {
        Pattern beginPattern = Pattern.compile("\\b(BEGIN|CASE)\\b", Pattern.CASE_INSENSITIVE);
        Pattern endPattern = Pattern.compile("\\bEND\\b", Pattern.CASE_INSENSITIVE);

        try (
            InputStreamReader sr = new InputStreamReader(st, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(sr);
        ) {
            List<String> sentencias = new ArrayList<>();
            String linea;
            String sentencia = "";
            int contador = 0;
            while((linea = br.readLine()) != null) {
                linea = linea.trim();
                if(linea.isEmpty()) continue;

                Matcher beginMatcher = beginPattern.matcher(linea);
                Matcher endMatcher = endPattern.matcher(linea);

                while(beginMatcher.find()) contador++;
                while(endMatcher.find()) contador--;

                sentencia += "\n" + linea;

                if(contador == 0 && linea.endsWith(";")) {
                    sentencias.add(sentencia);
                    sentencia = "";
                }
            }
            return sentencias;
        }
    }

    /**
     * Ejecuta un script SQL en la base de datos.
     * Las sentencias se ejecutan dentro de una transacción.
     * 
     * @param conn Conexión a la base de datos
     * @throws SQLException si hay errores en la ejecución del script
     * @throws IOException si hay errores al leer el script
     */
    private void executeSqlScript(Connection conn) throws SQLException, IOException {
        try (
            InputStream in = getClass().getClassLoader().getResourceAsStream(initScript);
            Statement stmt = conn.createStatement();
        ) {
            if (in == null) {
                throw new IOException("Init script not found: " + initScript);
            }

            conn.setAutoCommit(false);
            try {
                List<String> sentencias = splitSQL(in);
                for(String sentencia: sentencias) {
                    stmt.addBatch(sentencia);
                }
                stmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Obtiene la URL JDBC de conexión.
     * @return URL JDBC
     */
    public String getJdbcUrl() { return jdbcUrl; }

    /**
     * Obtiene la clase del driver JDBC.
     * @return Nombre de la clase del driver
     */
    public String getDriverClass() { return driverClass; }

    /**
     * Obtiene el tamaño mínimo del pool de conexiones.
     * @return Tamaño mínimo del pool
     */
    public int getMinPoolSize() { return minPoolSize; }

    /**
     * Obtiene el tamaño máximo del pool de conexiones.
     * @return Tamaño máximo del pool
     */
    public int getMaxPoolSize() { return maxPoolSize; }
}