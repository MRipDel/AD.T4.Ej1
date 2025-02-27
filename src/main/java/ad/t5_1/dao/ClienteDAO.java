package ad.t5_1.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;

import ad.t5_1.models.Cliente;
import ad.t5_1.models.ZonaEnvio;
import ad.t5_1.db.HibernateCP;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Implementación del DAO para la entidad Cliente usando Hibernate.
 */
public class ClienteDAO {
    
    /**
     * Obtiene todos los clientes.
     * @return Stream de todos los clientes
     */
    public Stream<Cliente> getAll() {
        Session session = HibernateCP.getInstance().getSessionFactory().openSession();
        return session.createQuery("FROM Cliente", Cliente.class)
            .stream()
            .onClose(() -> {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            });
    }

    /**
     * Obtiene un cliente por su ID.
     * @param id ID del cliente a buscar
     * @return Optional con el cliente si existe, Optional vacío si no
     */
    public Optional<Cliente> getById(Integer id) {
        try (Session session = HibernateCP.getInstance().getSessionFactory().openSession()) {
            return Optional.ofNullable(session.find(Cliente.class, id));
        }
    }

    /**
     * Guarda un nuevo cliente o actualiza uno existente.
     * @param cliente Cliente a guardar o actualizar
     */
    public void save(Cliente cliente) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateCP.getInstance().getSessionFactory().openSession();
            transaction = session.beginTransaction();
        // Aquí está la clave: si solo tenemos el ID de la zona, necesitamos cargar el objeto completo
        if (cliente.getZona() == null && cliente.getIdZona() > 0) {
            ZonaEnvio zona = session.find(ZonaEnvio.class, cliente.getIdZona());
            cliente.setZona(zona);
        }
            session.merge(cliente);
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
     * Elimina un cliente por su ID.
     * @param id ID del cliente a eliminar
     * @return true si se eliminó correctamente, false si no se encontró
     */
    public boolean delete(Integer id) {
        Transaction transaction = null;
        try (Session session = HibernateCP.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Cliente cliente = session.find(Cliente.class, id);
            if (cliente != null) {
                session.remove(cliente);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting client", e);
        }
    }

    /**
     * Encuentra clientes por zona.
     * @param zonaId ID de la zona
     * @return Stream de clientes que pertenecen a la zona especificada
     */
    public Stream<Cliente> getByZona(Integer zonaId) {
        Session session = HibernateCP.getInstance().getSessionFactory().openSession();
        return session.createQuery("FROM Cliente c WHERE c.zona.id = :zonaId", Cliente.class)
            .setParameter("zonaId", zonaId)
            .stream()
            .onClose(() -> {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            });
    }

    /**
     * Busca clientes por nombre.
     * @param nombre Parte del nombre a buscar
     * @return Stream de clientes cuyo nombre contiene el texto especificado
     */
    public Stream<Cliente> getByNombreContaining(String nombre) {
        Session session = HibernateCP.getInstance().getSessionFactory().openSession();
        return session.createQuery("FROM Cliente c WHERE c.nombre LIKE :nombre", Cliente.class)
            .setParameter("nombre", "%" + nombre + "%")
            .stream()
            .onClose(() -> {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            });
    }
}