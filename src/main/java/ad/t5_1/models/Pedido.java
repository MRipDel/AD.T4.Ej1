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

import java.time.LocalDate;

/**
 * Modela un pedido
 */
@Entity
@Table(name = "Pedidos")
public class Pedido implements Entidad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private int id;
    
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;
    
    @Column(name = "importe_total", nullable = false)
    private double importeTotal;
    
    // No anotamos este campo porque será manejado por la relación ManyToOne
    @Transient
    private int idCliente;
    
    // Agregamos la relación con Cliente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    /**
     * Constructor vacío
     */
    public Pedido() {
        super();
    }

    /**
     * Constructor con todos los parámetros
     * @param id
     * @param fecha
     * @param importeTotal
     * @param idCliente
     */
    public Pedido(int id, LocalDate fecha, double importeTotal, int idCliente) {
        this.id = id;
        this.fecha = fecha;
        this.importeTotal = importeTotal;
        this.idCliente = idCliente;
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

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public double getImporteTotal() {
        return importeTotal;
    }

    public void setImporteTotal(double importeTotal) {
        this.importeTotal = importeTotal;
    }

    // Mantenemos la interfaz original pero internamente trabaja con la relación
    public int getIdCliente() {
        return cliente != null ? cliente.getId() : idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
        // La asignación real del cliente se hará en el DAO
    }
    
    // Nuevos métodos para manejar la relación con Cliente
    public Cliente getCliente() {
        return cliente;
    }
    
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
        if (cliente != null) {
            this.idCliente = cliente.getId();
        }
    }
}