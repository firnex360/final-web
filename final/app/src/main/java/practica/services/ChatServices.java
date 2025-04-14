package practica.services;
import java.util.List;

import practica.logic.Chat;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class ChatServices extends GeneralClass<Chat> {
    

    private static ChatServices instance;

    private ChatServices(){
        super(Chat.class);
    }

    public static ChatServices getInstance(){
        if(instance==null){
            instance = new ChatServices();
        }
        return instance;
    }

    
    /**
     *
     * @param name
     * @return
     */
    public List<Chat> findAllById(String name){
        EntityManager em = getEntityManager();
        Query query = em.createQuery("select a from Chat a where a.id like :id");
        query.setParameter("id", name+"%");
        @SuppressWarnings("unpracticaed")
        List<Chat> list = query.getResultList();
        return list;
    }


}