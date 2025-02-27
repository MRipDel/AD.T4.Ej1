package ad.t5_1.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Modela un cliente
 */
@Entity
@Table(name = "Clientes")
public class Cliente implements Entidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private int id;
    
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;
    
    @Column(name = "email", nullable = false, length = 100)
    private String email;
    
    @Column(name = "telefono", length = 15)
    private String telefono;
    
    // No anotamos este campo porque será manejado por la relación ManyToOne
    @Transient
    private int idZona;
    
    // Agregamos la relación con ZonaEnvio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_zona", nullable = false)
    private ZonaEnvio zona;
    
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
        this.id = id;
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
    
    // Mantenemos la interfaz original pero internamente trabaja con la relación
    public int getIdZona() {
        return zona != null ? zona.getId() : idZona;
    }
    
    public void setIdZona(int idZona) {
        this.idZona = idZona;
        // La asignación real de la zona se hará en el DAO
    }
    
    // Nuevos métodos para manejar la relación con ZonaEnvio
    public ZonaEnvio getZona() {
        return zona;
    }
    
    public void setZona(ZonaEnvio zona) {
        this.zona = zona;
        if (zona != null) {
            this.idZona = zona.getId();
        }
    }
}