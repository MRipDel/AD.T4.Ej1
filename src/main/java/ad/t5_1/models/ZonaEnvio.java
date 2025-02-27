package ad.t5_1.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Modela una zona de envío
 */
@Entity
@Table(name = "Zonas_Envio")
public class ZonaEnvio implements Entidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_zona")
    private int id;
    
    @Column(name = "nombre_zona", nullable = false, length = 100)
    private String nombre;
    
    @Column(name = "tarifa_envio", nullable = false)
    private double tarifa;
    
    // Relación bidireccional con Cliente
    @OneToMany(mappedBy = "zona", cascade = CascadeType.ALL)
    private List<Cliente> clientes = new ArrayList<>();
    
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
        this.id = id;
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
    
    // Nuevos métodos para manejar la relación con Cliente
    public List<Cliente> getClientes() {
        return clientes;
    }
    
    public void setClientes(List<Cliente> clientes) {
        this.clientes = clientes;
    }
    
    public void addCliente(Cliente cliente) {
        clientes.add(cliente);
        cliente.setZona(this);
    }
    
    public void removeCliente(Cliente cliente) {
        clientes.remove(cliente);
        cliente.setZona(null);
    }
}