package practica.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import practica.logic.Article;
import practica.logic.Comment;
import practica.logic.Tag;
import practica.logic.User;

public class Head implements Serializable {
    public static final long serialVersionUID = 1L;
    public static List<User> users = null;
    public static List<Article> articles = null;
    public static List<Comment> comments = null;
    public static List<Tag> tags = null;
    public static Head head;

    public int id_article = 1;
    public int id_tag = 1;
    public int id_comment = 1;

    public Head() {
        this.users = new ArrayList<>();
        this.articles = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.tags = new ArrayList<>();

        //this.users.add(new User("manuelxd", "manuel", "123", false, false));
        //this.users.add(new User("mita123", "maria", "456", true, false));
        //this.users.add(new User("firnex", "fernando", "789", false, true));

    }

    public static Head getInstance() {
        if (head == null) {
            head = new Head();
        }
        return head;
    }

    public static List<User> getUsers() {
        return users;
    }

    public static void setUsers(List<User> users) {
        Head.users = users;
    }

    public static List<Article> getArticles() {
        return articles;
    }

    public static void setArticles(List<Article> articles) {
        Head.articles = articles;
    }

    public static List<Comment> getComments() {
        return comments;
    }

    public static void setComments(List<Comment> comments) {
        Head.comments = comments;
    }

    public static List<Tag> getTags() {
        return tags;
    }

    public static void setTags(List<Tag> tags) {
        Head.tags = tags;
    }

    public void add_user(User temp) {
        users.add(temp);
    }

    public void add_tag(String temp) {
        Tag aux = new Tag(0, temp);
        id_tag++;
        aux.setId(id_tag);
        tags.add(aux);
    }

    public void add_comment(Comment temp) {
        temp.setId(id_comment);
        id_comment++;
        comments.add(temp);
    }

    public User find_user(String username) {

        Optional<User> userEncontrado = users.stream()
                .filter(user -> username.equals(user.getUsername()))
                .findFirst();
                
        if (userEncontrado.isPresent()) {

            User user = userEncontrado.get();
            // Realizar acciones con el user encontrado
            return user;
        } else {

            return null;
        }

    }
   //====================================================================
   public Article findArticle(Long id) {

       for (Article article : articles) {
           if (article.getId() == id) {
               return article;
           }
       }

       return null; // Retorna null si no se encuentra ningún artículo con el ID proporcionado
   }

    public List<Article> listarArticles(){
        return articles;
    }

    public void add_article(Article temp) {
        temp.setId(id_article);
        id_article++;
        articles.add(temp);
    }
    public void updateArticle(Article temp, long id){

        Article aux = findArticle(id);
        int index = articles.indexOf(aux);

        articles.set(index, temp);
    }

    public void updateUser(String username, User tmp){
        User aux = find_user(username);
        int index = users.indexOf(aux);

        users.set(index, tmp);
    }

    public boolean deleteArticle(Long id) {

        for (int i = 0; i < articles.size(); i++) {
            Article article = articles.get(i);
            if (article.getId() == id) {
                articles.remove(i); // Elimina el artículo de la lista
                i--; // Ajusta el índice después de la eliminación
                return true;
            }
        }

        return false;
    }

    //===================================================================
    public List<Tag> listarTags(){
        return tags;
    }
    public Comment find_comment (Long id){
        return comments.stream().filter(comment -> id.equals(comment.getId())).findFirst().orElse(null);
    }

    public Tag find_Tag (String nametag){
        return tags.stream().filter(tag -> nametag.equals(tag.getName())).findFirst().orElse(null);
    }

    public List<User> listarUsers(){
        return users;
    }

    public List<Comment> listarComments(){
        return comments;
    }

    public boolean userExiste(String username) {
        return users.stream().anyMatch(user -> user.getUsername().equals(username));
    }

    public boolean validPassword(String password, User user){
        return user.getPassword().equals(password);
    }

    public boolean deleteUser(String username) {

        User temp = find_user(username);

        if (temp != null && !temp.getUsername().equalsIgnoreCase("admin")) {
            users.remove(temp);
            return true;
        }
        else{
            System.out.println("couldn't find user");
            return false;
        }
    }

    //Miscellaneous methods

    public List<Tag> separateTags(String tagsString){

        if (tagsString == null || tagsString.isEmpty()) {
            return null;
        }

        List<Tag> tmpList = new ArrayList<>();;
        String[] regexStrings = tagsString.split(",");

        for (String string : regexStrings) {
            Tag tag = new Tag(id_tag, string);
            id_tag++;
            tmpList.add(tag);
            tags.add(tag);
        }

        return tmpList;

    }

    public String concatenatedTags(List<Tag> tagList){

        if (tagList == null || tagList.isEmpty()) {
            return null;
        }

        String tagString = "";

        for (Tag tag : tagList) {
            tagString = tagString.concat(tag.getName());
            tagString = tagString.concat(", ");
        }

        return tagString;

    }


}