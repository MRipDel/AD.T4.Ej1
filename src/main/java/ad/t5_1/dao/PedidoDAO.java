package ad.t5_1.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Optional;
import java.util.stream.Stream;

import ad.t5_1.db.HibernateSessionManager;
import ad.t5_1.models.Cliente;
import ad.t5_1.models.Pedido;

/**
 * Implementación del DAO para Pedido usando Hibernate pero manteniendo 
 * la interfaz original Crud<Pedido>.
 */
public class PedidoDAO implements Crud<Pedido> {
    
    /**
     * Obtiene todos los pedidos de la base de datos.
     * @return Un Stream de objetos Pedido.
     */
    @Override
    public Stream<Pedido> get() {
        Session session = HibernateSessionManager.getInstance().getSessionFactory().openSession();
        return session.createQuery("FROM Pedido", Pedido.class)
            .stream()
            .onClose(() -> {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            });
    }

    /**
     * Obtiene un pedido específico por su ID.
     * @param id El ID del pedido a buscar.
     * @return Un Optional que contiene el pedido si existe.
     */
    @Override
    public Optional<Pedido> get(int id) {
        try (Session session = HibernateSessionManager.getInstance().getSessionFactory().openSession()) {
            return Optional.ofNullable(session.find(Pedido.class, id));
        }
    }

    /**
     * Inserta un nuevo pedido en la base de datos.
     * @param pedido El pedido a insertar.
     */
    @Override
    public void insert(Pedido pedido) {
        Transaction transaction = null;
        try (Session session = HibernateSessionManager.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Manejar la relación con Cliente
            if (pedido.getCliente() == null && pedido.getIdCliente() > 0) {
                Cliente cliente = session.find(Cliente.class, pedido.getIdCliente());
                if (cliente != null) {
                    pedido.setCliente(cliente);
                }
            }
            
            session.persist(pedido);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    System.err.println("Error durante rollback: " + rollbackEx.getMessage());
                }
            }
            throw new RuntimeException("Error inserting order", e);
        }
    }

    /**
     * Elimina un pedido de la base de datos.
     * @param id El ID del pedido a eliminar.
     * @return true si el pedido fue eliminado, false si no se encontró.
     */
    @Override
    public boolean delete(int id) {
        Transaction transaction = null;
        try (Session session = HibernateSessionManager.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Pedido pedido = session.find(Pedido.class, id);
            if (pedido != null) {
                session.remove(pedido);
                transaction.commit();
                return true;
            }
            transaction.commit();
            return false;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    System.err.println("Error durante rollback: " + rollbackEx.getMessage());
                }
            }
            throw new RuntimeException("Error deleting order", e);
        }
    }

    /**
     * Actualiza los datos de un pedido existente.
     * @param pedido El pedido con los datos actualizados.
     * @return true si el pedido fue actualizado, false si no se encontró.
     */
    @Override
    public boolean update(Pedido pedido) {
        Transaction transaction = null;
        try (Session session = HibernateSessionManager.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            Pedido existingPedido = session.find(Pedido.class, pedido.getId());
            if (existingPedido == null) {
                transaction.commit();
                return false;
            }
            
            // Manejar la relación con Cliente
            if (pedido.getCliente() == null && pedido.getIdCliente() > 0) {
                Cliente cliente = session.find(Cliente.class, pedido.getIdCliente());
                if (cliente != null) {
                    pedido.setCliente(cliente);
                }
            }
            
            session.merge(pedido);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    System.err.println("Error durante rollback: " + rollbackEx.getMessage());
                }
            }
            throw new RuntimeException("Error updating order", e);
        }
    }

    /**
     * Actualiza el ID de un pedido.
     * @param oldId El ID actual del pedido.
     * @param newId El nuevo ID para el pedido.
     * @return true si el ID fue actualizado, false si no se encontró el pedido.
     */
    @Override
    public boolean update(int oldId, int newId) {
        Transaction transaction = null;
        try (Session session = HibernateSessionManager.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            Pedido pedido = session.find(Pedido.class, oldId);
            if (pedido == null) {
                transaction.commit();
                return false;
            }
            
            // Verificar que no exista ya un pedido con el nuevo ID
            if (session.find(Pedido.class, newId) != null) {
                transaction.commit();
                return false;
            }
            
            // Crear una copia con el nuevo ID
            Pedido nuevoPedido = new Pedido();
            nuevoPedido.setId(newId);
            nuevoPedido.setFecha(pedido.getFecha());
            nuevoPedido.setImporteTotal(pedido.getImporteTotal());
            nuevoPedido.setCliente(pedido.getCliente());
            
            // Eliminar el original y persistir el nuevo
            session.remove(pedido);
            session.persist(nuevoPedido);
            
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    System.err.println("Error durante rollback: " + rollbackEx.getMessage());
                }
            }
            throw new RuntimeException("Error updating order ID", e);
        }
    }

    /**
     * Recupera todos los pedidos de un cliente específico.
     * @param idCliente Identificador del cliente
     * @return Stream de pedidos del cliente
     */
    public Stream<Pedido> getPedidosPorCliente(int idCliente) {
        Session session = HibernateSessionManager.getInstance().getSessionFactory().openSession();
        return session.createQuery(
                "FROM Pedido p WHERE p.cliente.id = :idCliente ORDER BY p.fecha DESC", 
                Pedido.class)
            .setParameter("idCliente", idCliente)
            .stream()
            .onClose(() -> {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            });
    }

    /**
     * Calcula el importe total de todos los pedidos de un cliente.
     * @param idCliente Identificador del cliente
     * @return Total gastado por el cliente
     */
    public double getTotalGastadoPorCliente(int idCliente) {
        try (Session session = HibernateSessionManager.getInstance().getSessionFactory().openSession()) {
            Query<Double> query = session.createQuery(
                "SELECT SUM(p.importeTotal) FROM Pedido p WHERE p.cliente.id = :idCliente", 
                Double.class
            );
            query.setParameter("idCliente", idCliente);
            Double result = query.uniqueResult();
            return result != null ? result : 0.0;
        }
    }
}