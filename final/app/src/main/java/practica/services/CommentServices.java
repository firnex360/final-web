package practica.services;
import java.util.List;

import practica.logic.Comment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class CommentServices extends GeneralClass<Comment> {
    

    private static CommentServices instance;

    private CommentServices(){
        super(Comment.class);
    }

    public static CommentServices getInstance(){
        if(instance==null){
            instance = new CommentServices();
        }
        return instance;
    }

    
    /**
     *
     * @param name
     * @return
     */
    public List<Comment> findAllById(String name){
        EntityManager em = getEntityManager();
        Query query = em.createQuery("select a from Comment a where a.id like :id");
        query.setParameter("id", name+"%");
        @SuppressWarnings("unpracticaed")
        List<Comment> list = query.getResultList();
        return list;
    }


}