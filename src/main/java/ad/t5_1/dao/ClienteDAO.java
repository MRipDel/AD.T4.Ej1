package ad.t5_1.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Optional;
import java.util.stream.Stream;

import ad.t5_1.db.HibernateSessionManager;
import ad.t5_1.models.Cliente;
import ad.t5_1.models.ZonaEnvio;

/**
 * Implementación del DAO para Cliente usando Hibernate pero manteniendo 
 * la interfaz original Crud<Cliente>.
 */
public class ClienteDAO implements Crud<Cliente> {
    
    /**
     * Obtiene todos los clientes de la base de datos.
     * @return Un Stream de objetos Cliente.
     */
    @Override
    public Stream<Cliente> get() {
        Session session = HibernateSessionManager.getInstance().getSessionFactory().openSession();
        return session.createQuery("FROM Cliente", Cliente.class)
            .stream()
            .onClose(() -> {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            });
    }

    /**
     * Obtiene un cliente específico por su ID.
     * @param id El ID del cliente a buscar.
     * @return Un Optional que contiene el cliente si existe.
     */
    @Override
    public Optional<Cliente> get(int id) {
        try (Session session = HibernateSessionManager.getInstance().getSessionFactory().openSession()) {
            return Optional.ofNullable(session.find(Cliente.class, id));
        }
    }

    /**
     * Inserta un nuevo cliente en la base de datos.
     * @param cliente El cliente a insertar.
     */
    @Override
    public void insert(Cliente cliente) {
        Transaction transaction = null;
        try (Session session = HibernateSessionManager.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Manejar la relación con ZonaEnvio
            if (cliente.getZona() == null && cliente.getIdZona() > 0) {
                ZonaEnvio zona = session.find(ZonaEnvio.class, cliente.getIdZona());
                if (zona != null) {
                    cliente.setZona(zona);
                }
            }
            
            session.persist(cliente);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    // Log el error de rollback pero no lo propagamos
                    System.err.println("Error durante rollback: " + rollbackEx.getMessage());
                }
            }
            throw new RuntimeException("Error inserting client", e);
        }
    }

    /**
     * Elimina un cliente de la base de datos.
     * @param id El ID del cliente a eliminar.
     * @return true si el cliente fue eliminado, false si no se encontró.
     */
    @Override
    public boolean delete(int id) {
        Transaction transaction = null;
        try (Session session = HibernateSessionManager.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Cliente cliente = session.find(Cliente.class, id);
            if (cliente != null) {
                session.remove(cliente);
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
            throw new RuntimeException("Error deleting client", e);
        }
    }

    /**
     * Actualiza los datos de un cliente existente.
     * @param cliente El cliente con los datos actualizados.
     * @return true si el cliente fue actualizado, false si no se encontró.
     */
    @Override
    public boolean update(Cliente cliente) {
        Transaction transaction = null;
        try (Session session = HibernateSessionManager.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            Cliente existingCliente = session.find(Cliente.class, cliente.getId());
            if (existingCliente == null) {
                transaction.commit();
                return false;
            }
            
            // Manejar la relación con ZonaEnvio
            if (cliente.getZona() == null && cliente.getIdZona() > 0) {
                ZonaEnvio zona = session.find(ZonaEnvio.class, cliente.getIdZona());
                if (zona != null) {
                    cliente.setZona(zona);
                }
            }
            
            session.merge(cliente);
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
            throw new RuntimeException("Error updating client", e);
        }
    }

    /**
     * Actualiza el ID de un cliente.
     * @param oldId El ID actual del cliente.
     * @param newId El nuevo ID para el cliente.
     * @return true si el ID fue actualizado, false si no se encontró el cliente.
     */
    @Override
    public boolean update(int oldId, int newId) {
        Transaction transaction = null;
        try (Session session = HibernateSessionManager.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // En Hibernate no es sencillo cambiar un ID directamente
            // Tenemos que obtener el objeto, crear uno nuevo con el ID nuevo, y eliminar el original
            Cliente cliente = session.find(Cliente.class, oldId);
            if (cliente == null) {
                transaction.commit();
                return false;
            }
            
            // Verificar que no exista ya un cliente con el nuevo ID
            if (session.find(Cliente.class, newId) != null) {
                transaction.commit();
                return false;
            }
            
            // Crear una copia con el nuevo ID
            Cliente nuevoCliente = new Cliente();
            nuevoCliente.setId(newId);
            nuevoCliente.setNombre(cliente.getNombre());
            nuevoCliente.setEmail(cliente.getEmail());
            nuevoCliente.setTelefono(cliente.getTelefono());
            nuevoCliente.setZona(cliente.getZona());
            
            // Eliminar el original y persistir el nuevo
            session.remove(cliente);
            session.persist(nuevoCliente);
            
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
            throw new RuntimeException("Error updating client ID", e);
        }
    }
}