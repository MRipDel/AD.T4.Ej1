package ad.t5_1.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import ad.t5_1.models.Cliente;
import ad.t5_1.models.Pedido;
import ad.t5_1.db.HibernateCP;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Implementación del DAO para la entidad Pedido usando Hibernate.
 */
public class PedidoDAO {
    
    /**
     * Obtiene todos los pedidos.
     * @return Stream de todos los pedidos
     */
    public Stream<Pedido> getAll() {
        Session session = HibernateCP.getInstance().getSessionFactory().openSession();
        return session.createQuery("FROM Pedido", Pedido.class)
            .stream()
            .onClose(() -> {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            });
    }

    /**
     * Obtiene un pedido por su ID.
     * @param id ID del pedido a buscar
     * @return Optional con el pedido si existe, Optional vacío si no
     */
    public Optional<Pedido> getById(Integer id) {
        try (Session session = HibernateCP.getInstance().getSessionFactory().openSession()) {
            return Optional.ofNullable(session.find(Pedido.class, id));
        }
    }

    /**
     * Guarda un nuevo pedido o actualiza uno existente.
     * @param pedido Pedido a guardar o actualizar
     */
    public void save(Pedido pedido) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateCP.getInstance().getSessionFactory().openSession();
            transaction = session.beginTransaction();
        if (pedido.getCliente() == null && pedido.getIdCliente() > 0) {
            Cliente cliente = session.find(Cliente.class, pedido.getIdCliente());
            pedido.setCliente(cliente);
        }
            session.merge(pedido);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error saving client", e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Elimina un pedido por su ID.
     * @param id ID del pedido a eliminar
     * @return true si se eliminó correctamente, false si no se encontró
     */
    public boolean delete(Integer id) {
        Transaction transaction = null;
        try (Session session = HibernateCP.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Pedido pedido = session.find(Pedido.class, id);
            if (pedido != null) {
                session.remove(pedido);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting order", e);
        }
    }

    /**
     * Encuentra pedidos por cliente.
     * @param clienteId ID del cliente
     * @return Stream de pedidos del cliente especificado
     */
    public Stream<Pedido> getByCliente(Integer clienteId) {
        Session session = HibernateCP.getInstance().getSessionFactory().openSession();
        return session.createQuery("FROM Pedido p WHERE p.cliente.id = :clienteId ORDER BY p.fecha DESC", Pedido.class)
            .setParameter("clienteId", clienteId)
            .stream()
            .onClose(() -> {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            });
    }

    /**
     * Encuentra pedidos por fecha.
     * @param fecha Fecha de los pedidos a buscar
     * @return Stream de pedidos realizados en la fecha especificada
     */
    public Stream<Pedido> getByFecha(LocalDate fecha) {
        Session session = HibernateCP.getInstance().getSessionFactory().openSession();
        return session.createQuery("FROM Pedido p WHERE p.fecha = :fecha", Pedido.class)
            .setParameter("fecha", fecha)
            .stream()
            .onClose(() -> {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            });
    }

    /**
     * Calcula el total gastado por un cliente.
     * @param clienteId ID del cliente
     * @return Total gastado por el cliente
     */
    public double getTotalGastadoPorCliente(Integer clienteId) {
        try (Session session = HibernateCP.getInstance().getSessionFactory().openSession()) {
            Query<Double> query = session.createQuery(
                "SELECT SUM(p.importeTotal) FROM Pedido p WHERE p.cliente.id = :clienteId", 
                Double.class);
            query.setParameter("clienteId", clienteId);
            Double result = query.uniqueResult();
            return result != null ? result : 0.0;
        }
    }
}