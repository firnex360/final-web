package practica.services;
import java.util.List;

import practica.logic.Article;
import practica.logic.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class ArticleServices extends GeneralClass<Article> {
    

    private static ArticleServices instance;

    private ArticleServices(){
        super(Article.class);
    }

    public static ArticleServices getInstance(){
        if(instance==null){
            instance = new ArticleServices();
        }
        return instance;
    }

    

    // Method to fetch articles with pagination in articleservices
    public List<Article> findArticlesWithPagination(int page, int pageSize) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT a FROM Article a ORDER BY a.id", Article.class)
                    .setFirstResult((page - 1) * pageSize) // Offset
                    .setMaxResults(pageSize) // Limit
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // Method to count total articles (for pagination controls)
    public long countArticles() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(a) FROM Article a", Long.class)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    
    /**
     *
     * @param name
     * @return
     */
    public List<Article> findAllById(String name){
        EntityManager em = getEntityManager();
        Query query = em.createQuery("select a from Article a where a.id like :id");
        query.setParameter("id", name+"%");
        @SuppressWarnings("unpracticaed")
        List<Article> list = query.getResultList();
        return list;
    }


}