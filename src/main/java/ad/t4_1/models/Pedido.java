package ad.t4_1.models;
/**
 * @author Manuel Ripalda Delgado
 */
import java.time.LocalDate;

import ad.t4_1.interfaces.Entity;
/**
 * Modela un pedido
 */
public class Pedido implements Entity{
    private int id;
    private LocalDate fecha;
    private double importeTotal;
    private int idCliente;

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
        this.id=id;
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

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }
}
