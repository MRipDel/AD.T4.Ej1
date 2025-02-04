package ad.t4_1.db;

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
 * Clase para configurar las propiedades de la base de datos. Usa métodos de SqlUtils
 */
public class SQLiteConfig {
    private String jdbcUrl;
    private String driverClass;
    private int minPoolSize;
    private int maxPoolSize;
    private String dbName;
    private String initScript;
    
    public static final int CONNECTION_TIMEOUT = 30000; // 30 segundos

    public SQLiteConfig(String dbPath, Optional<String> initScriptPath) {
        this.dbName = dbPath;
        this.initScript = initScriptPath.orElse(null);
        this.jdbcUrl = "jdbc:sqlite:" + dbPath;
        this.driverClass = "org.sqlite.JDBC";
        this.minPoolSize = 5;
        this.maxPoolSize = 10;
        
        initializeIfNeeded();
    }

    private void initializeIfNeeded() {
        Path dbPath = Path.of(dbName);
        if (!Files.exists(dbPath)) {
            createDatabase();
        }
    }

    private void createDatabase() {
        try {
            try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
                executeSqlScript(conn);
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Error initializing database", e);
        }
    }

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

    // Getters necesarios
    public String getJdbcUrl() { return jdbcUrl; }
    public String getDriverClass() { return driverClass; }
    public int getMinPoolSize() { return minPoolSize; }
    public int getMaxPoolSize() { return maxPoolSize; }
}