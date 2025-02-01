package ad.t4_1.dao;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import ad.t4_1.models.Entity;

/**
 * Interfaz que define las operaciones CRUD básicas
 * que deben implementar todas las clases DAO.
 */
public interface Crud<T extends Entity> {

    /**
     * Obtiene un objeto a partir de su identificador.
     * @param id El identificador del objeto que se quiere recuperar.
     * @return El objeto recuperado.
     * @throws DataAccessException
     */
    public Optional<T> get(int id);
    /**
     * Obtiene todos los objetos de un determinado tipo.
     * @return EL flujo de objetos.
     */
    public Stream<T> get();

    /**
     * Borrar un objeto del almacenamiento a partir de su identificador.
     * @param id El identificador del objeto.
     * @return true, si se logró borrar el objeto.
     */
    public boolean delete(int id);
    /**
     * Borrar un objeto del almacenamiento.
     * @param obj El objeto que se quiere eliminar.
     * @return true, si se logro borrar el objeto.
     */
    default boolean delete(T obj) {
        return delete(obj.getId());
    }

    /**
     * Agrega el objeto al almacenamiento.
     * @param obj El objeto que quiere almacenarse.
     */
    public void insert(T obj);
    /**
     * Agrega varios objeto al almacenamiento.
     * @param objs Los objetos a almacenar.
     */
    default void insert(Iterable<T> objs) {
        for(T obj: objs) insert(obj);
    }
    /**
     * Agrega varios objeto al almacenamiento.
     * @param objs Los objetos a almacenar.
     */
    default void insert(T[] obj) {
        insert(Arrays.asList(obj));
    }

    /**
     * Actualiza un objeto en el almacenamiento. El identificador NO puede
     * haber cambiado.
     * @param obj El objeto a cambiar.
     * @return true, si se logró hacer la actualización.
     */
    public boolean update(T obj);
    /**
     * Modifica el identificador de un objeto en el almacenamiento.
     * @param oldId El antiguo identificador.
     * @param newId El nuevo identificador.
     * @return true, si se logró hacer la actualización.
     */
    public boolean update(int oldId, int newId);
    /**
     * Modifica el identificador de un objeto en el almacenamiento.
     * @param obj El objeto cuyo identificador se quiere modificar.
     * @param newId EL nuevo identificador.
     * @return true, si se logró la actualización.
     */
    default boolean update(T obj, int newId){
        return update(obj.getId(), newId);
    }
}