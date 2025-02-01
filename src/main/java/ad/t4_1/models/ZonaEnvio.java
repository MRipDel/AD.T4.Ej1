package ad.t4_1.models;
/**
 * @author Manuel Ripalda Delgado
 */
/**
 * Modela una zona de envío
 */
public class ZonaEnvio implements Entity{
    private int id;
    private String nombre;
    private double tarifa;
    
    /**
     * Constructor vacío
     */
    public ZonaEnvio() {
        super();
    }

    /**
     * Constructor con todos los parámetros
     * @param id
     * @param nombre
     * @param tarifa
     */
    public ZonaEnvio(int id, String nombre, double tarifa) {
        this.id = id;
        this.nombre = nombre;
        this.tarifa = tarifa;
    }

    /* Getters y setters */

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id=id;    
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getTarifa() {
        return tarifa;
    }

    public void setTarifa(double tarifa) {
        this.tarifa = tarifa;
    }
}
