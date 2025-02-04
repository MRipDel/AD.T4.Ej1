package ad.t4_1.ui;

import java.util.Map;
import java.util.HashMap;

/**
 * Fábrica para crear instancias de interfaces de usuario.
 * Implementa el patrón Factory Method permitiendo registrar y crear
 * diferentes tipos de interfaces de usuario de manera dinámica.
 */
public class UIFactory {
    /** Constante para la interfaz de consola */
    public static final String UI_CONSOLE = "console";
    
    /** Constante para la interfaz gráfica (futura implementación) */
    public static final String UI_GUI = "gui";
    
    /** Constante para la interfaz automática (futura implementación) */
    public static final String UI_AUTO = "auto";
    
    /** Mapa que almacena los tipos de interfaces disponibles */
    private static final Map<String, Class<? extends UserInterface>> uiTypes = new HashMap<>();
    
    /**
     * Inicializa el mapa de tipos de interfaces disponibles.
     * Se pueden agregar más tipos de interfaces en el futuro.
     */
    static {
        uiTypes.put(UI_CONSOLE, Console.class);
        // Futuros tipos de UI se registrarían aquí
        // uiTypes.put(UI_GUI, GUI.class);
        // uiTypes.put(UI_AUTO, AutomaticUI.class);
    }
    
    /**
     * Crea una instancia de la interfaz de usuario especificada.
     * 
     * @param type Tipo de interfaz a crear (case-insensitive)
     * @return Una nueva instancia de la interfaz de usuario solicitada
     * @throws IllegalArgumentException si el tipo de interfaz no está soportado
     * @throws RuntimeException si hay errores al crear la instancia
     */
    public static UserInterface createUI(String type) {
        Class<? extends UserInterface> uiClass = uiTypes.get(type.toLowerCase());
        if (uiClass == null) {
            throw new IllegalArgumentException("Tipo de UI no soportado: " + type);
        }
        
        try {
            return uiClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Error creando la UI: " + e.getMessage(), e);
        }
    }
    
    /**
     * Registra un nuevo tipo de interfaz de usuario.
     * Permite agregar dinámicamente nuevos tipos de interfaces.
     * 
     * @param type Identificador del tipo de interfaz (case-insensitive)
     * @param uiClass Clase que implementa la interfaz de usuario
     */
    public static void registerUIType(String type, Class<? extends UserInterface> uiClass) {
        uiTypes.put(type.toLowerCase(), uiClass);
    }
}