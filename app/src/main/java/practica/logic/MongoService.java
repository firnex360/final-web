package practica.logic;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class MongoService {
    private final MongoClient client;
    private final MongoDatabase db;

    public MongoService() {
        this.client = MongoClients.create(
                "mongodb+srv://fernandorodriguezb27:8eLDve6oHLmBiTPa@cluster-test.gn22mgw.mongodb.net/?retryWrites=true&w=majority&appName=Cluster-test");
        this.db = client.getDatabase("final");
    }

    public MongoDatabase getDb() {
        return db;
    }

    //User
    public void saveUser(User user) {
        Document doc = new Document("username", user.getUsername())
                .append("name", user.getName())
                .append("admin", user.isAdmin())
                .append("author", user.isAuthor());

        db.getCollection("users").insertOne(doc);
    }

    public User getUser(String username) {
        Document doc = db.getCollection("users").find(Filters.eq("username", username)).first();
        if (doc == null)
            return null;
        return new User(doc.getString("username"), doc.getString("name"), null, doc.getBoolean("admin"),
                doc.getBoolean("author"), null);
    }

    //ShortURL 
    public long getNextShortURLId() {
        Document last = db.getCollection("shorturls").find().sort(Sorts.descending("id")).first();
        return last == null ? 1 : last.getLong("id") + 1;
    }

    public void saveShortURL(ShortURL url) {
        Document doc = new Document("id", url.getId())
                .append("shortUrl", url.getShortUrl())
                .append("originalUrl", url.getOriginalUrl())
                .append("username", url.getUser().getUsername())
                .append("accessCount", url.getAccessCount());
        db.getCollection("shorturls").insertOne(doc);
    }

    public ShortURL getShortURL(String shortUrl) {
        Document doc = db.getCollection("shorturls").find(Filters.eq("shortUrl", shortUrl)).first();
        if (doc == null)
            return null;
        User user = getUser(doc.getString("username"));
        return new ShortURL(doc.getLong("id"), doc.getString("shortUrl"), doc.getString("originalUrl"), user,
                doc.getInteger("accessCount"), new ArrayList<>());
    }

    public ShortURL getShortURLById(Long id) {
        Document doc = db.getCollection("shorturls").find(Filters.eq("id", id)).first();
        if (doc == null)
            return null;
        User user = getUser(doc.getString("username"));
        return new ShortURL(doc.getLong("id"), doc.getString("shortUrl"), doc.getString("originalUrl"), user,
                doc.getInteger("accessCount"), new ArrayList<>());
    }

    public List<ShortURL> getShortURLsByUsername(String username) {
        MongoCollection<Document> collection = db.getCollection("shorturls");
    
        List<ShortURL> results = new ArrayList<>();
    
        for (Document doc : collection.find(Filters.eq("username", username))) {
            results.add(ShortURL.fromDocument(doc));
        }
        
        if (results.isEmpty()) {
            return null;
        }
        return results;
    }

    public List<ShortURL> getAllShortURLs() {
        MongoCollection<Document> collection = db.getCollection("shorturls");
    
        List<ShortURL> results = new ArrayList<>();
        for (Document doc : collection.find()) {
            results.add(ShortURL.fromDocument(doc));
        }
    
        return results;
    }

    public void incrementAccessCount(Long id) {

        MongoCollection<Document> collection = db.getCollection("shorturls");

        Bson filter = Filters.eq("id", id);
        Bson update = Updates.inc("accessCount", 1);
        collection.updateOne(filter, update);
    }

    public boolean deleteShortURLById(Long id) {

        MongoCollection<Document> collection = db.getCollection("shorturls");

        Bson filter = Filters.eq("id", id);
        DeleteResult result = collection.deleteOne(filter);

        // Optional: also delete access logs related to this URL
        //Bson accessLogFilter = Filters.eq("urlId", id);
        //collection.deleteMany(accessLogFilter);

        return result.getDeletedCount() > 0;
    }
    
    //URLAccessLog 
    public long getNextAccessLogId() {
        Document last = db.getCollection("accesslogs").find().sort(Sorts.descending("id")).first();
        return last == null ? 1 : last.getLong("id") + 1;
    }

    public void saveAccessLog(URLAccessLog log) {
        Document doc = new Document("id", log.getId())
                .append("urlId", log.getUrlEntry().getId())
                .append("ip", log.getIp())
                .append("browser", log.getBrowser())
                .append("clientDomain", log.getClientDomain())
                .append("os", log.getOs())
                .append("accessTime", log.getAccessTime().toString());
        db.getCollection("accesslogs").insertOne(doc);
    }

    public List<URLAccessLog> getAccessLogsByUrlId(long urlId, ShortURL shortURL) {
        List<URLAccessLog> logs = new ArrayList<>();
        MongoCollection<Document> coll = db.getCollection("accesslogs");
        FindIterable<Document> docs = coll.find(Filters.eq("urlId", urlId));

        for (Document doc : docs) {
            logs.add(new URLAccessLog(
                    doc.getLong("id"),
                    shortURL,
                    doc.getString("ip"),
                    doc.getString("browser"),
                    doc.getString("clientDomain"),
                    doc.getString("os"),
                    java.time.LocalDateTime.parse(doc.getString("accessTime"))));
        }
        return logs;
    }

    //testing connection
    public void testConnection() {
        System.out.println("Collections in 'test2' database:");
        for (String name : db.listCollectionNames()) {
            System.out.println("- " + name);
        }

        System.out.println("\nInserting a test document into 'testing' collection...");
        db.getCollection("testing").insertOne(new Document("message", "Hello from Java!"));
        System.out.println("Document inserted!");
    }
}
