package ad.t4_1.ui;

import java.util.Map;
import java.util.HashMap;

public class UIFactory {
    public static final String UI_CONSOLE = "console";
    public static final String UI_GUI = "gui";
    public static final String UI_AUTO = "auto";
    
    private static final Map<String, Class<? extends UserInterface>> uiTypes = new HashMap<>();
    
    static {
        uiTypes.put(UI_CONSOLE, Console.class);
        // Futuros tipos de UI se registrarían aquí
        // uiTypes.put(UI_GUI, GUI.class);
        // uiTypes.put(UI_AUTO, AutomaticUI.class);
    }
    
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
    
    public static void registerUIType(String type, Class<? extends UserInterface> uiClass) {
        uiTypes.put(type.toLowerCase(), uiClass);
    }
}