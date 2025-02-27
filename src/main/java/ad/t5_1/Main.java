package ad.t5_1;

import ad.t5_1.db.HibernateCP;
import ad.t5_1.ui.UIFactory;
import ad.t5_1.ui.UserInterface;

/**
 * Clase principal de la aplicación de gestión de pedidos.
 */
public class Main {
    /** Tipo de interfaz de usuario por defecto */
    private static final String DEFAULT_UI = "console";
    
    /** Ruta por defecto para la base de datos */
    private static final String DEFAULT_DB_PATH = "pedidos.db";
    
    /** Archivo de configuración de Hibernate por defecto */
    private static final String HIBERNATE_CONFIG = "hibernate.cfg.xml";

    /**
     * Punto de entrada principal de la aplicación.
     */
    public static void main(String[] args) {
        String uiType = DEFAULT_UI;
        String dbPath = DEFAULT_DB_PATH;
        String hibernateConfig = HIBERNATE_CONFIG;

        // Procesar argumentos de la línea de comandos
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--ui":
                    if (i + 1 < args.length) {
                        uiType = args[++i];
                    }
                    break;
                case "--db":
                    if (i + 1 < args.length) {
                        dbPath = args[++i];
                        // Configurar la URL de la base de datos para Hibernate
                        System.setProperty("hibernate.connection.url", "jdbc:sqlite:" + dbPath);
                    }
                    break;
                case "--hibernate-config":
                    if (i + 1 < args.length) {
                        hibernateConfig = args[++i];
                    }
                    break;
                case "--init-script":
                    // Ignoramos este parámetro ya que la inicialización se maneja en hibernate.cfg.xml
                    if (i + 1 < args.length) i++; // Skip the next argument
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
            // Inicializar Hibernate
            HibernateCP.getInstance(hibernateConfig);
            
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
            // Asegurar que el SessionFactory se cierra
            try {
                HibernateCP.getInstance().closeSessionFactory();
            } catch (Exception e) {
                System.err.println("Error cerrando Hibernate: " + e.getMessage());
            }
        }
    }

    /**
     * Muestra información sobre el uso correcto del programa.
     */
    private static void printUsage() {
        System.out.println("Uso: java -jar programa.jar [opciones]");
        System.out.println("Opciones:");
        System.out.println("  --db <path>                 Ruta a la base de datos (default: pedidos.db)");
        System.out.println("  --ui <tipo>                 Tipo de interfaz de usuario (default: console)");
        System.out.println("  --hibernate-config <path>   Archivo de configuración de Hibernate (default: hibernate.cfg.xml)");
    }
}