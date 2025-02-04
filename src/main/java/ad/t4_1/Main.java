package ad.t4_1;

import com.fasterxml.jackson.databind.ObjectMapper;
import ad.t4_1.db.SQLiteConnectionPool;
import ad.t4_1.ui.UIFactory;
import ad.t4_1.ui.UserInterface;
import java.io.File;
import java.util.Optional;
import java.util.Map;

public class Main {
    private static final String DEFAULT_DB_PATH = "pedidos.db";
    private static final String DEFAULT_INIT_SCRIPT = "pedidos.sql";
    private static final String DEFAULT_UI = "console";
    
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        String uiType = DEFAULT_UI;
        String dbPath = DEFAULT_DB_PATH;
        String initScript = DEFAULT_INIT_SCRIPT;
        
        try {
            // Intentar cargar configuración desde launch.json si existe
            File configFile = new File("launch.json");
            if (configFile.exists()) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> config = mapper.readValue(configFile, Map.class);
                
                // Obtener tipo de UI
                uiType = (String) config.getOrDefault("ui", DEFAULT_UI);
                
                // Obtener configuración de base de datos
                Map<String, String> dbConfig = (Map<String, String>) config.getOrDefault("database", Map.of());
                dbPath = dbConfig.getOrDefault("path", DEFAULT_DB_PATH);
                initScript = dbConfig.getOrDefault("init_script", DEFAULT_INIT_SCRIPT);
            }
            
            // Inicializar base de datos
            SQLiteConnectionPool.getInstance(dbPath, Optional.of(initScript));
            
            // Crear y ejecutar la interfaz de usuario
            UserInterface ui = UIFactory.createUI(uiType);
            try {
                ui.start();
            } finally {
                ui.stop();
            }
        } catch (Exception e) {
            System.err.println("Error en la aplicación: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            // Asegurar que el pool de conexiones se cierra
            try {
                SQLiteConnectionPool.getInstance().closePool();
            } catch (Exception e) {
                System.err.println("Error cerrando el pool de conexiones: " + e.getMessage());
            }
        }
    }
}