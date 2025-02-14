package ad.t5_1.models;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Pedidos")
public class Pedido{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private int id;
    
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;
    
    @Column(name = "importe_total", nullable = false, precision = 10, scale = 2)
    private double importeTotal;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    public Pedido() {
        super();
    }

    public Pedido(int id, LocalDate fecha, double importeTotal, int idCliente) {
        this.id = id;
        this.fecha = fecha;
        this.importeTotal = importeTotal;
    }

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
    
    public int getIdCliente() {
        return cliente != null ? cliente.getId() : 0;
    }
    
    public void setIdCliente(int idCliente) {
        // Este método se mantiene por compatibilidad pero no se usa directamente con JPA
    }
}