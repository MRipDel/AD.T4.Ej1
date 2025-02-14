package ad.t5_1.db;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestiona la configuración y sesiones de Hibernate utilizando el patrón Singleton.
 * Esta clase es responsable de la inicialización de Hibernate y proporciona acceso
 * centralizado al SessionFactory.
 */
public class HibernateCP {
    
    /** Logger para registrar eventos y errores */
    private static final Logger LOGGER = Logger.getLogger(HibernateCP.class.getName());
    
    /** Instancia única del gestor de sesiones */
    private static volatile HibernateCP instance;
    
    /** Factory de sesiones de Hibernate */
    private final SessionFactory sessionFactory;

    /**
     * Constructor privado que inicializa la configuración de Hibernate.
     */
    private HibernateCP() {
        try {
            this.sessionFactory = new Configuration()
                .configure() // Lee hibernate.cfg.xml
                .buildSessionFactory();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing Hibernate SessionFactory", e);
            throw new RuntimeException("Could not initialize Hibernate SessionFactory", e);
        }
    }

    /**
     * Obtiene la instancia única del gestor de sesiones.
     * 
     * @return Instancia de HibernateCP
     */
    public static synchronized HibernateCP getInstance() {
        if (instance == null) {
            synchronized (HibernateCP.class) {
                if (instance == null) {
                    instance = new HibernateCP();
                }
            }
        }
        return instance;
    }

    /**
     * Obtiene el SessionFactory de Hibernate.
     * 
     * @return SessionFactory configurado
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Cierra el SessionFactory y libera los recursos.
     * También reinicia la instancia singleton.
     */
    public void closeSessionFactory() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            try {
                sessionFactory.close();
                instance = null;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing SessionFactory", e);
            }
        }
    }

    /**
     * Verifica si el SessionFactory está cerrado.
     * 
     * @return true si el SessionFactory está cerrado, false en caso contrario
     */
    public boolean isClosed() {
        return sessionFactory == null || sessionFactory.isClosed();
    }

    /**
     * Evita la clonación del singleton.
     * 
     * @throws CloneNotSupportedException siempre, para prevenir la clonación
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("HibernateCP cannot be cloned");
    }
}
