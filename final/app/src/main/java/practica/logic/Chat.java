package practica.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
public class Chat implements Serializable{
    @Id
    private Long id;
    private User authorAdmin;
    private User sendUser;
    private String senderName;
    @ElementCollection
    private List<String> authorAdminMessages = new ArrayList<>();
    @ElementCollection
    private List<String> senderMessages = new ArrayList<>();
    
    public Chat(){
        
    }

    public Chat(Long id, User authorAdmin, User sendUser, String senderName, List<String> authorAdminMessages, List<String> senderMessages) {
        this.id = id;
        this.authorAdmin = authorAdmin;
        this.sendUser = sendUser;
        this.senderName = senderName;
        this.authorAdminMessages = (authorAdminMessages != null) ? authorAdminMessages : new ArrayList<>();
        this.senderMessages = (senderMessages != null) ? senderMessages : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getAuthorAdmin() {
        return authorAdmin;
    }

    public void setAuthorAdmin(User authorAdmin) {
        this.authorAdmin = authorAdmin;
    }

    public User getSendUser() {
        return sendUser;
    }

    public void setSendUser(User sendUser) {
        this.sendUser = sendUser;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public List<String> getAuthorAdminMessages() {
        return authorAdminMessages;
    }

    public void setAuthorAdminMessages(List<String> authorAdminMessages) {
        this.authorAdminMessages = authorAdminMessages;
    }

    public List<String> getSenderMessages() {
        return senderMessages;
    }

    public void setSenderMessages(List<String> senderMessages) {
        this.senderMessages = senderMessages;
    }

        


}
