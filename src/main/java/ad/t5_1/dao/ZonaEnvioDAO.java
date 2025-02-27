package ad.t5_1.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;

import ad.t5_1.models.ZonaEnvio;
import ad.t5_1.db.HibernateCP;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Implementación del DAO para la entidad ZonaEnvio usando Hibernate.
 */
public class ZonaEnvioDAO {
    
    /**
     * Obtiene todas las zonas de envío.
     * @return Stream de todas las zonas de envío
     */
    public Stream<ZonaEnvio> getAll() {
        Session session = HibernateCP.getInstance().getSessionFactory().openSession();
        return session.createQuery("FROM ZonaEnvio", ZonaEnvio.class)
            .stream()
            .onClose(() -> {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            });
    }

    /**
     * Obtiene una zona de envío por su ID.
     * @param id ID de la zona a buscar
     * @return Optional con la zona si existe, Optional vacío si no
     */
    public Optional<ZonaEnvio> getById(Integer id) {
        try (Session session = HibernateCP.getInstance().getSessionFactory().openSession()) {
            return Optional.ofNullable(session.find(ZonaEnvio.class, id));
        }
    }

    /**
     * Guarda una nueva zona de envío o actualiza una existente.
     * @param zonaEnvio Zona de envío a guardar o actualizar
     */
    public void save(ZonaEnvio zonaEnvio) {
        Transaction transaction = null;
        Session session = null;
        try {
            session = HibernateCP.getInstance().getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.merge(zonaEnvio);
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
     * Elimina una zona de envío por su ID.
     * @param id ID de la zona a eliminar
     * @return true si se eliminó correctamente, false si no se encontró
     */
    public boolean delete(Integer id) {
        Transaction transaction = null;
        try (Session session = HibernateCP.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Verificar si hay clientes asociados antes de eliminar
            Long clientCount = session.createQuery(
                "SELECT COUNT(c) FROM Cliente c WHERE c.zona.id = :zonaId", 
                Long.class)
                .setParameter("zonaId", id)
                .uniqueResult();
                
            if (clientCount > 0) {
                throw new RuntimeException("Cannot delete shipping zone with associated clients");
            }
            
            ZonaEnvio zonaEnvio = session.find(ZonaEnvio.class, id);
            if (zonaEnvio != null) {
                session.remove(zonaEnvio);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting shipping zone", e);
        }
    }

    /**
     * Obtiene todas las zonas con el número de clientes en cada una.
     * @return Stream de zonas con sus clientes precargados
     */
    public Stream<ZonaEnvio> getZonasConNumeroClientes() {
        Session session = HibernateCP.getInstance().getSessionFactory().openSession();
        return session.createQuery(
            "SELECT DISTINCT z FROM ZonaEnvio z LEFT JOIN FETCH z.clientes", 
            ZonaEnvio.class)
            .stream()
            .onClose(() -> {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            });
    }

    /**
     * Busca zonas de envío por nombre.
     * @param nombre Parte del nombre a buscar
     * @return Stream de zonas cuyo nombre contiene el texto especificado
     */
    public Stream<ZonaEnvio> findByNombreContaining(String nombre) {
        Session session = HibernateCP.getInstance().getSessionFactory().openSession();
        return session.createQuery(
            "FROM ZonaEnvio z WHERE LOWER(z.nombre) LIKE LOWER(:nombre)", 
            ZonaEnvio.class)
            .setParameter("nombre", "%" + nombre + "%")
            .stream()
            .onClose(() -> {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            });
    }

    /**
     * Obtiene la tarifa de envío de una zona específica.
     * @param zonaId ID de la zona
     * @return Tarifa de envío de la zona
     */
    public double getTarifa(Integer zonaId) {
        try (Session session = HibernateCP.getInstance().getSessionFactory().openSession()) {
            Double tarifa = session.createQuery(
                "SELECT z.tarifa FROM ZonaEnvio z WHERE z.id = :zonaId",
                Double.class)
                .setParameter("zonaId", zonaId)
                .uniqueResult();
            return tarifa != null ? tarifa : 0.0;
        }
    }
}