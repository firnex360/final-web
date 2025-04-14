package practica.services;
import java.util.List;

import practica.logic.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class TagServices extends GeneralClass<Tag> {
    

    private static TagServices instance;

    private TagServices(){
        super(Tag.class);
    }

    public static TagServices getInstance(){
        if(instance==null){
            instance = new TagServices();
        }
        return instance;
    }

    
    /**
     *
     * @param name
     * @return
     */
    public List<Tag> findAllById(String name){
        EntityManager em = getEntityManager();
        Query query = em.createQuery("select a from Tag a where a.id like :id");
        query.setParameter("id", name+"%");
        @SuppressWarnings("unpracticaed")
        List<Tag> list = query.getResultList();
        return list;
    }


}