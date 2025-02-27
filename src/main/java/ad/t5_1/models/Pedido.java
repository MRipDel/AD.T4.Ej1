package ad.t5_1.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Pedidos")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private int id;
    
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;
    
    @Column(name = "importe_total", nullable = false)
    private double importeTotal;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    // Constructor sin parámetros
    public Pedido() {
        this.fecha = LocalDate.now();
    }

    // Constructor completo
    public Pedido(int id, LocalDate fecha, double importeTotal, Cliente cliente) {
        this.id = id;
        this.fecha = fecha;
        this.importeTotal = importeTotal;
        this.cliente = cliente;
    }
    // Constructor sin el id
    public Pedido( LocalDate fecha, double importeTotal, Cliente cliente) {
        this.fecha = fecha;
        this.importeTotal = importeTotal;
        this.cliente = cliente;
    }
    // Getters y setters
    public int getId() {
        return id;
    }

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

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
    
    // Método para compatibilidad con la versión anterior
    public int getIdCliente() {
        return cliente != null ? cliente.getId() : 0;
    }
    
    // Método para compatibilidad con la versión anterior
    public void setIdCliente(int idCliente) {
        // Este método se mantiene por compatibilidad
    }
}