package practica.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.persistence.*;


@Entity
public class Article implements Serializable {
    @Id
    private long id;
    private String title;
    private String body;
    @ManyToOne
    private User author;
    private Date date;
    @OneToMany
    private List<Comment> comments = new ArrayList<Comment>();
    @ManyToMany
    private List<Tag> tags = new ArrayList<Tag>();

    public Article() {
    }

    public Article(long id, String title, String body, User author, Date date, List<Comment> comments, List<Tag> tags) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.author = author;
        this.date = date;
        this.comments = (comments != null) ? comments : new ArrayList<>();
        this.tags = (tags != null) ? tags : new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public void addComment(Comment comment) {
        System.out.println(comment.getComment());
        this.comments.add(comment);
    }

    public void removeComment(Comment comment) {
        this.comments.remove(comment);
    }

}
