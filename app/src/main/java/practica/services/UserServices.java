package practica.services;

import java.util.List;

import jakarta.persistence.EntityManager;
import practica.logic.User;

public class UserServices extends GeneralClass<User> {

    private static UserServices instance;

    private UserServices() {
        super(User.class);
    }

    public static UserServices getInstance() {
        if (instance == null) {
            instance = new UserServices();
        }
        return instance;
    }


    // Method to fetch users with pagination in userservices
    public List<User> findUsersWithPagination(int page, int pageSize) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT u FROM User u ORDER BY u.username", User.class)
                    .setFirstResult((page - 1) * pageSize) // Offset
                    .setMaxResults(pageSize) // Limit
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // Method to count total users (for pagination controls)
    public long countUsers() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(u) FROM User u", Long.class)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }


    /**
     *
     * @param username
     * @return
     */
    public User findByUsername(String username) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (Exception e) {
            return null; // User not found
        } finally {
            em.close();
        }
    }

}