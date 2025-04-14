package practica.logic;

import java.io.Serializable;

import jakarta.persistence.*;

@Entity
@Table (name = "Usuario")
public class User implements Serializable{
    @Id
    private String username;
    private String name;
    private String password;
    private boolean admin;
    private boolean author;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "foto_id", referencedColumnName = "id")
    private Image profilePicture;
    
    public User(){
        
    }

    public User(String username, String name, String password, boolean admin, boolean author, Image profilePicture) {
        this.username = username;
        this.name = name;
        this.password = password;
        this.admin = admin;
        this.author = author;
        this.profilePicture = profilePicture;
    }
    
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public boolean isAdmin() {
        return admin;
    }
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
    public boolean isAuthor() {
        return author;
    }
    public void setAuthor(boolean author) {
        this.author = author;
    }

    public Image getProfilePicture() { 
        return profilePicture; 
    }
    public void setProfilePicture(Image profilePicture) { 
        this.profilePicture = profilePicture; 
    }

}


