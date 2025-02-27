package ad.t5_1.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Zonas_Envio")
public class ZonaEnvio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_zona")
    private int id;
    
    @Column(name = "nombre_zona", nullable = false, length = 100)
    private String nombre;
    
    @Column(name = "tarifa_envio", nullable = false)
    private double tarifa;
    
    @OneToMany(mappedBy = "zona", cascade = CascadeType.ALL)
    private List<Cliente> clientes = new ArrayList<>();

    // Constructor sin parámetros
    public ZonaEnvio() {
        super();
    }

    // Constructor completo
    public ZonaEnvio(int id, String nombre, double tarifa) {
        this.id = id;
        this.nombre = nombre;
        this.tarifa = tarifa;
    }

    // Getters y setters
    public int getId() {
        return id;
    }

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
    
    public List<Cliente> getClientes() {
        return clientes;
    }
    
    public void setClientes(List<Cliente> clientes) {
        this.clientes = clientes;
    }
    
    // Métodos de ayuda para gestionar la relación bidireccional
    public void addCliente(Cliente cliente) {
        clientes.add(cliente);
        cliente.setZona(this);
    }
    
    public void removeCliente(Cliente cliente) {
        clientes.remove(cliente);
        cliente.setZona(null);
    }
}