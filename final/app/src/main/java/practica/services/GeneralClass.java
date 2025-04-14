package practica.services;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Id;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.criteria.CriteriaQuery;
import practica.Main;

@Entity
public class GeneralClass<T> {

    // private static final Map<Class<?>, Map<Object, Object>> database = new
    // HashMap<>();
    private static EntityManagerFactory emf;
    private final Class<T> entityClass;

    public GeneralClass(Class<T> entityClass) {

        if (emf == null) {
            if (Main.getConnectionMethod().equalsIgnoreCase("Heroku")) {
                emf = getConfiguracionBaseDatosHeroku();
            } else {
                emf = Persistence.createEntityManagerFactory("PersistenceUnit");
            }
        }
        this.entityClass = entityClass;
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    private EntityManagerFactory getConfiguracionBaseDatosHeroku() {
        // Leyendo la información de la variable de ambiente de Heroku
        String databaseUrl = System.getenv("DATABASE_URL");
        StringTokenizer st = new StringTokenizer(databaseUrl, ":@/");
        // Separando las información del conexión.
        String dbVendor = st.nextToken();
        String userName = st.nextToken();
        String password = st.nextToken();
        String host = st.nextToken();
        String port = st.nextToken();
        String databaseName = st.nextToken();
        // creando la jbdc String
        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s?sslmode=require", host, port, databaseName);
        // pasando las propiedades.
        Map<String, String> properties = new HashMap<>();
        properties.put("javax.persistence.jdbc.url", jdbcUrl);
        properties.put("javax.persistence.jdbc.user", userName);
        properties.put("javax.persistence.jdbc.password", password);
        //
        return Persistence.createEntityManagerFactory("Heroku", properties);
    }

    /**
     * Metodo para obtener el valor del campo anotado como @ID.
     * 
     * @param entity
     * @return
     */
    private Object getCampValue(T entity) {
        if (entity == null) {
            return null;
        }
        // aplicando la clase de reflexión.
        for (Field f : entity.getClass().getDeclaredFields()) { // tomando todos los campos privados.
            if (f.isAnnotationPresent(Id.class)) { // preguntando por la anotación ID.
                try {
                    f.setAccessible(true);
                    Object campValue = f.get(entity);

                    System.out.println("Nombre del campo: " + f.getName());
                    System.out.println("Tipo del campo: " + f.getType().getName());
                    System.out.println("Valor del campo: " + campValue);

                    return campValue;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    /**
     *
     * @param entity
     */
    public T create(T entity) throws IllegalArgumentException, EntityExistsException, PersistenceException {
        EntityManager em = getEntityManager();

        try {

            em.getTransaction().begin();
            em.persist(entity);
            em.getTransaction().commit();

        } finally {
            em.close();
        }
        return entity;
    }

    /**
     * @param entity
     * @return
     */
    public T edit(T entity) throws PersistenceException {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        try {
            em.merge(entity);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return entity;
    }

    /**
     * @param entityId
     * @return
     */
    public boolean delete(Object entityId) throws PersistenceException {
        boolean ok = false;
        EntityManager em = getEntityManager();
        em.getTransaction().begin();
        try {
            T entity = em.find(entityClass, entityId);
            em.remove(entity);
            em.getTransaction().commit();
            ok = true;
        } finally {
            em.close();
        }
        
        return ok;
    }

    /**
     * @param id
     * @return
     */
    public T find(Object id) throws PersistenceException {

        if (id == null) {
            return null;
        }

        EntityManager em = getEntityManager();
        try {
            return em.find(entityClass, id);
        } finally {
            em.close();
        }
    }

    /**
     * @return
     */
    public List<T> findAll() throws PersistenceException {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery<T> criteriaQuery = em.getCriteriaBuilder().createQuery(entityClass);
            criteriaQuery.select(criteriaQuery.from(entityClass));
            return em.createQuery(criteriaQuery).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * @return
     */
    public Long getNextId() {
        EntityManager em = getEntityManager();
        try {
            Long maxId = em.createQuery(
                "SELECT MAX(e.id) FROM " + entityClass.getSimpleName() + " e", Long.class
            ).getSingleResult();
            return (maxId == null) ? 1L : maxId + 1;
        } finally {
            em.close();
        }
    }
    

}
