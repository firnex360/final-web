package practica.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import practica.services.UserServices;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;

// @Entity
// @Table(name = "urls")
public class ShortURL implements Serializable {
    //@Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //@Column(nullable = false, unique = true)
    @JsonBackReference
    private String shortUrl;

    //@Column(nullable = false)
    private String originalUrl;

    private User user;

    //@Column(nullable = false)
    private int accessCount = 0;

    //@OneToMany(mappedBy = "urlEntry", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<URLAccessLog> accessLogs = new ArrayList<>();

    public ShortURL(Long id, String shortUrl, String originalUrl, User user, int accessCount, List<URLAccessLog> accessLogs) {
        this.id = id;
        this.shortUrl = shortUrl;
        this.originalUrl = originalUrl;
        this.user = user;
        this.accessCount = accessCount;
        this.accessLogs = (accessLogs != null) ? accessLogs : new ArrayList<>();
    }

    public ShortURL() {
        this.accessCount = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }  
    
    public int getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(int accessCount) {
        this.accessCount = accessCount;
    }

    public List<URLAccessLog> getAccessLogs() {
        return accessLogs;
    }

    public void setAccessLogs(List<URLAccessLog> accessLogs) {
        this.accessLogs = accessLogs;
    }

    public static ShortURL fromDocument(Document doc) {
        ShortURL shortUrl = new ShortURL();
    
        shortUrl.setId(doc.getLong("id"));
        shortUrl.setOriginalUrl(doc.getString("originalUrl"));
        shortUrl.setShortUrl(doc.getString("shortUrl"));
        shortUrl.setAccessCount(doc.getInteger("accessCount"));
    
        // Handle user object if it exists
        Document userDoc = doc.get("user", Document.class);
        if (userDoc != null) {
            User user = new User();
            user.setUsername(userDoc.getString("username"));
            shortUrl.setUser(user);
        }
        else {
            //comment this out cause it wasn't safe to show all the credential of the user in a api

            // User user = new User();
            // user = UserServices.getInstance().findByUsername(doc.getString("username"));

            // if (user == null) {
            //     user = new User();
            //     user.setUsername(doc.getString("username"));
            // }

            // shortUrl.setUser(user);
        }

        return shortUrl;
    }

}
