package ad.t4_1.ui;

/**
 * Interfaz base para todas las implementaciones de interfaz de usuario.
 * Define el contrato básico que deben cumplir todas las interfaces de
 * usuario en la aplicación, independientemente de su tipo (consola, 
 * gráfica, automática, etc.).
 */
public interface UserInterface {
    
    /**
     * Inicia la interfaz de usuario.
     * Este método debe contener la lógica principal de la interfaz,
     * incluyendo el bucle principal de ejecución si es necesario.
     */
    void start();
    
    /**
     * Detiene la interfaz de usuario.
     * Debe encargarse de la limpieza y liberación de recursos
     * antes de finalizar la ejecución.
     */
    void stop();
}