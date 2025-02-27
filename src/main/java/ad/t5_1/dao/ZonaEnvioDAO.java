package ad.t5_1.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Optional;
import java.util.stream.Stream;

import ad.t5_1.db.HibernateSessionManager;
import ad.t5_1.models.ZonaEnvio;

/**
 * Implementación del DAO para ZonaEnvio usando Hibernate pero manteniendo 
 * la interfaz original Crud<ZonaEnvio>.
 */
public class ZonaEnvioDAO implements Crud<ZonaEnvio> {
    
    /**
     * Obtiene todas las zonas de envío de la base de datos.
     * @return Un Stream de objetos ZonaEnvio.
     */
    @Override
    public Stream<ZonaEnvio> get() {
        Session session = HibernateSessionManager.getInstance().getSessionFactory().openSession();
        return session.createQuery("FROM ZonaEnvio", ZonaEnvio.class)
            .stream()
            .onClose(() -> {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            });
    }

    /**
     * Obtiene una zona de envío específica por su ID.
     * @param id El ID de la zona a buscar.
     * @return Un Optional que contiene la zona si existe.
     */
    @Override
    public Optional<ZonaEnvio> get(int id) {
        try (Session session = HibernateSessionManager.getInstance().getSessionFactory().openSession()) {
            return Optional.ofNullable(session.find(ZonaEnvio.class, id));
        }
    }

    /**
     * Inserta una nueva zona de envío en la base de datos.
     * @param zonaEnvio La zona a insertar.
     */
    @Override
    public void insert(ZonaEnvio zonaEnvio) {
        Transaction transaction = null;
        try (Session session = HibernateSessionManager.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(zonaEnvio);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                try {
                    transaction.rollback();
                } catch (Exception rollbackEx) {
                    System.err.println("Error durante rollback: " + rollbackEx.getMessage());
                }
            }
            throw new RuntimeException("Error inserting shipping zone", e);
        }
    }

    /**
     * Elimina una zona de envío de la base de datos.
     * @param id El ID de la zona a eliminar.
     * @return true si la zona fue eliminada, false si no se encontró.
     */
    @Override
    public boolean delete(int id) {
        Transaction transaction = null;
        try (Session session = HibernateSessionManager.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Verificar si hay clientes asociados antes de eliminar
            Long clientCount = session.createQuery(
                "SELECT COUNT(c) FROM Cliente c WHERE c.zona.id = :zonaId", 
                Long.class)
                .setParameter("zonaId", id)
                .uniqueResult();
                
            if (clientCount > 0) {
                // No podemos eliminar si hay clientes asociados
                transaction.commit();
                return false;
            }
            
            ZonaEnvio zonaEnvio = session.find(ZonaEnvio.class, id);
            if (zonaEnvio != null) {
                session.remove(zonaEnvio);
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
            throw new RuntimeException("Error deleting shipping zone", e);
        }
    }

    /**
     * Actualiza los datos de una zona de envío existente.
     * @param zonaEnvio La zona con los datos actualizados.
     * @return true si la zona fue actualizada, false si no se encontró.
     */
    @Override
    public boolean update(ZonaEnvio zonaEnvio) {
        Transaction transaction = null;
        try (Session session = HibernateSessionManager.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            ZonaEnvio existingZona = session.find(ZonaEnvio.class, zonaEnvio.getId());
            if (existingZona == null) {
                transaction.commit();
                return false;
            }
            
            session.merge(zonaEnvio);
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
            throw new RuntimeException("Error updating shipping zone", e);
        }
    }

    /**
     * Actualiza el ID de una zona de envío.
     * @param oldId El ID actual de la zona.
     * @param newId El nuevo ID para la zona.
     * @return true si el ID fue actualizado, false si no se encontró la zona.
     */
    @Override
    public boolean update(int oldId, int newId) {
        Transaction transaction = null;
        try (Session session = HibernateSessionManager.getInstance().getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Verificar que no exista ya una zona con el nuevo ID
            ZonaEnvio existingWithNewId = session.find(ZonaEnvio.class, newId);
            if (existingWithNewId != null) {
                transaction.commit();
                return false;
            }
            
            ZonaEnvio zona = session.find(ZonaEnvio.class, oldId);
            if (zona == null) {
                transaction.commit();
                return false;
            }
            
            // Crear una copia con el nuevo ID
            ZonaEnvio nuevaZona = new ZonaEnvio();
            nuevaZona.setId(newId);
            nuevaZona.setNombre(zona.getNombre());
            nuevaZona.setTarifa(zona.getTarifa());
            
            // Eliminar el original y persistir el nuevo
            session.remove(zona);
            session.persist(nuevaZona);
            
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
            throw new RuntimeException("Error updating shipping zone ID", e);
        }
    }
    
    /**
     * Obtiene todas las zonas de envío junto con el número de clientes en cada zona.
     * @return Stream de objetos ZonaEnvio con información sobre sus clientes
     */
    public Stream<ZonaEnvio> getZonasConNumeroClientes() {
        Session session = HibernateSessionManager.getInstance().getSessionFactory().openSession();
        
        // Con Hibernate podemos obtener las zonas con sus clientes precargados
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
}