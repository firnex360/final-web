package practica.logic;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.*;
import practica.logic.Article;
import practica.logic.User;

@Entity
public class Comment implements Serializable {
    @Id
    private long id;
    private String comment;
    @ManyToOne
    private User author;
    @ManyToOne
    private Article article;
    private Date date;

    public Comment() {
    }
    
    public Comment(long id, String comment, User author, Article article, Date date) {
        this.id = id;
        this.comment = comment;
        this.author = author;
        this.article = article;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    

}
