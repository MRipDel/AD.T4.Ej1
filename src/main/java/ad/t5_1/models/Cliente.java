package ad.t5_1.models;
/**
 * @author Manuel Ripalda Delgado
 */
/**
 * Modela un cliente
 */
public class Cliente implements Entity{
    private int id;
    private String nombre;
    private String email;
    private String telefono;
    private int idZona;
    
    /**
     * Constructor sin parámetros
     */
    public Cliente() {
        super();
    }

    /**
     * Constructor con todos los parámetros
     * @param id
     * @param nombre
     * @param email
     * @param telefono
     * @param idZona
     */
    public Cliente(int id, String nombre, String email, String telefono, int idZona) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.idZona = idZona;
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
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public int getIdZona() {
        return idZona;
    }
    
    public void setIdZona(int idZona) {
        this.idZona = idZona;
    }
    
    
    
}
