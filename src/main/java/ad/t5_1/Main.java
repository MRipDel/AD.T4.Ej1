package ad.t5_1;

import java.util.Optional;

import ad.t5_1.db.SQLiteConnectionPool;
import ad.t5_1.ui.UIFactory;
import ad.t5_1.ui.UserInterface;

/**
 * Clase principal de la aplicación de gestión de pedidos.
 * Se encarga de inicializar los componentes necesarios, procesar argumentos
 * de la línea de comandos y gestionar el ciclo de vida de la aplicación.
 */
public class Main {
    /** Ruta por defecto para la base de datos */
    private static final String DEFAULT_DB_PATH = "pedidos.db";
    
    /** Script SQL por defecto para inicialización */
    private static final String DEFAULT_INIT_SCRIPT = "pedidos.sql";
    
    /** Tipo de interfaz de usuario por defecto */
    private static final String DEFAULT_UI = "console";

    /**
     * Punto de entrada principal de la aplicación.
     * 
     * @param args Argumentos de la línea de comandos:
     *            --db: ruta a la base de datos
     *            --init-script: ruta al script de inicialización
     *            --ui: tipo de interfaz de usuario
     */
    public static void main(String[] args) {
        String dbPath = DEFAULT_DB_PATH;
        String initScript = DEFAULT_INIT_SCRIPT;
        String uiType = DEFAULT_UI;

        // Procesar argumentos de la línea de comandos
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--db":
                    if (i + 1 < args.length) {
                        dbPath = args[++i];
                    }
                    break;
                case "--init-script":
                    if (i + 1 < args.length) {
                        initScript = args[++i];
                    }
                    break;
                case "--ui":
                    if (i + 1 < args.length) {
                        uiType = args[++i];
                    }
                    break;
                default:
                    if (args[i].startsWith("--")) {
                        System.err.println("Argumento desconocido: " + args[i]);
                        printUsage();
                        System.exit(1);
                    }
            }
        }

        try {
            // Inicializar la base de datos
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

    /**
     * Muestra información sobre el uso correcto del programa.
     * Imprime las opciones disponibles y su sintaxis.
     */
    private static void printUsage() {
        System.out.println("Uso: java -jar programa.jar [opciones]");
        System.out.println("Opciones:");
        System.out.println("  --db <path>           Ruta a la base de datos (default: pedidos.db)");
        System.out.println("  --init-script <path>  Script SQL de inicialización (default: pedidos.sql)");
        System.out.println("  --ui <tipo>           Tipo de interfaz de usuario (default: console)");
    }
}