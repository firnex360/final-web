package practica;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.javalin.Javalin;
import io.javalin.http.UploadedFile;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import io.javalin.security.RouteRole;
import io.javalin.websocket.WsContext;
import practica.logic.Article;
import practica.logic.Chat;
import practica.logic.Comment;
import practica.logic.Image;
import practica.logic.MongoService;
import practica.logic.ShortURL;
import practica.logic.Tag;
import practica.logic.URLAccessLog;
import practica.logic.User;
import practica.logic.ApiController;
import practica.services.ArticleServices;
import practica.services.BootStrapServices;
import practica.services.Head;
import practica.services.TagServices;
import practica.services.UserServices;
import static io.javalin.apibuilder.ApiBuilder.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import practica.grpc.ShortUrlRnServiceGrpc;



public class Main {

    private static String connectionMethod = "";

    // initializing mongoDB
    private static MongoService mongoService = new MongoService();

    // for chat functinality
    public static List<WsContext> userConnected = new ArrayList<>();
    private static ObjectMapper objectMapper = new ObjectMapper(); // Jackson JSON Mapper
    private static List<Chat> chatList = new ArrayList<>();
    private static Article currentArticle = null;

    // for url shortener functionality
    private static final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static List<ShortURL> urlList = new ArrayList<>();

    enum Rules implements RouteRole {
        ANONYMOUS,
        USER,
    }

    // @SuppressWarnings("CallToPrintStackTrace")
    @SuppressWarnings("unused")
    public static void main(String[] args) throws IOException {

        System.out.println("ORM-JPA METHOD THIS TIME");

        // Iniciando la base de datos.
        if (connectionMethod.isEmpty()) {
            BootStrapServices.getInstance().init();
        }

        try {

            // ceating defualt values to test
            // users
            UserServices.getInstance().create(new User("admin", "admin", "admin", true, true, null));
            UserServices.getInstance().create(new User("mita", "maria", "123", false, true, null));
            UserServices.getInstance().create(new User("firnex", "fernando", "456", true, false, null));
            UserServices.getInstance().create(new User("1", "1", "1", true, true, null));
            UserServices.getInstance().create(new User("2", "2", "2", false, false, null));
            UserServices.getInstance().create(new User("3", "3", "3", true, false, null));

            // articles
            ArticleServices.getInstance().create(new Article(1, "What is Lorem Ipsum?",
                    "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book.",
                    UserServices.getInstance().find("admin"), new Date(), null, null));
            ArticleServices.getInstance().create(new Article(2, "The FitnessGram PACER Test",
                    "The FitnessGram PACER Test is a multistage aerobic capacity test that progressively gets more difficult as it continues.",
                    UserServices.getInstance().find("mita"), new Date(), null, null));
            ArticleServices.getInstance().create(new Article(3, "Just for you.",
                    "Above life their that gathered itself under in you're us lesser heaven said. Their heaven air brought appear night darkness third waters had his sea it Face fish, seed creeping set. Man it tree set.",
                    UserServices.getInstance().find("firnex"), new Date(), null, null));
            ArticleServices.getInstance().create(new Article(4, "Seasons!",
                    "Seasons likeness multiply bring made every lesser isn't first air, green greater image gathered good that divide, kind doesn't wherein they're lesser grass subdue.",
                    UserServices.getInstance().find("admin"), new Date(), null, null));
            ArticleServices.getInstance().create(new Article(5, "Article 5", "This is the body of the article 5",
                    UserServices.getInstance().find("mita"), new Date(), null, null));
            ArticleServices.getInstance().create(new Article(6, "Article 6", "This is the body of the article 6",
                    UserServices.getInstance().find("firnex"), new Date(), null, null));

            // tags
            TagServices.getInstance().create(new Tag(1, "Thisisagagsdf"));
            TagServices.getInstance().create(new Tag(2, "Historia"));
            TagServices.getInstance().create(new Tag(3, "Geografía"));

        } catch (Exception e) {
            System.out.println("couldnt create default values");
            System.out.println(e.getMessage());
        }

        //#####################################################################
        //GRPC
        int grpcPort = 5000;
        Server server = ServerBuilder.forPort(grpcPort)
            .addService(new ShortUrlRnServiceGrpc())
            .build()
            .start();

        System.out.println("############## \nServidor gRPC iniciando y escuchando en puerto: 5000\n##############");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gRPC server");
            server.shutdown();
        }));
        //#####################################################################

        Javalin app = Javalin.create(/* HABILITAR RUTA ESTATICA */ javalinConfig -> {
            javalinConfig.staticFiles.add("/public", Location.CLASSPATH);
            javalinConfig.fileRenderer(new JavalinThymeleaf());

            // OpenAPI Configuration
            javalinConfig.router.apiBuilder(() -> {
                path("/api",() -> {
                    before(ApiController::jwtFilter);
                    path("/user/{username}", () -> {
                        get(ApiController::getUrlsByUser);
                        post(ApiController::createShortUrlAPI);
                        // path("/{original}", () -> {
                        //     post(ApiController::createShortUrlAPI);
                        // });
                    });
                });
            });
            
        }).start(7000);

        app.ws("/chat", ws -> {

            ws.onConnect(ctx -> {
                System.out.println("A user connected! " + ctx.sessionId());
                userConnected.add(ctx);
                User user = ctx.sessionAttribute("USER");

                List<Chat> userChats = new ArrayList<Chat>();

                for (Chat chat : chatList) {
                    if (chat.getAuthorAdmin().getUsername().equals(user.getUsername()) ||
                            chat.getSendUser().getUsername().equals(user.getUsername())) {
                        userChats.add(chat);
                    }
                }

                // Convert chat list to JSON
                JsonArray chatArray = new JsonArray();
                for (Chat chat : userChats) {
                    JsonObject chatJson = new JsonObject();
                    chatJson.addProperty("id", chat.getId());
                    chatJson.addProperty("name",
                            (chat.getSendUser() != null) ? chat.getSendUser().getUsername() : "unknown");
                    chatArray.add(chatJson);
                }

                JsonObject chatListMessage = new JsonObject();
                chatListMessage.addProperty("type", "chatList"); // Identify message type
                chatListMessage.add("chats", chatArray);

                // Send chat list to the connected user
                ctx.send(chatListMessage.toString());

            });

            ws.onMessage(ctx -> {
                User authorAdmin = null;
                User user = ctx.sessionAttribute("USER");
                List<String> messagesList = null;
                String senderName = new String();
                String message = ctx.message();
                String senderId = ctx.sessionId();
                System.out.println("Received: " + message + ". From: " + user.getUsername());

                try {

                    JsonNode json = objectMapper.readTree(message);
                    // Extracting values from JSON
                    String type = json.get("type").asText();

                    if ("sendMessage".equals(type)) {
                        System.out.println("received json type");
                        // int chatId = json.get("chatId").asInt(); // Get chatId
                        String text = json.get("text").asText();

                        message = text;

                    }

                    if ("openChat".equals(type)) {
                        System.out.println("received json type");
                        // int chatId = json.get("chatId").asInt(); // Get chatId
                        String text = "<- this user connected!";

                        message = text;

                    }

                } catch (Exception e) {
                    System.out.println("> send messages: " + e.getMessage());
                }

                if (currentArticle != null && currentArticle.getAuthor() != null) {
                    authorAdmin = currentArticle.getAuthor();
                }

                // may be potencial problems when retriving chat in the list article view
                Chat chatExists = doesChatExist(authorAdmin, user, senderName);

                if (chatExists != null) {
                    if (chatExists.getSendUser().getUsername().equalsIgnoreCase(user.getUsername())
                            || chatExists.getSenderName().equalsIgnoreCase(senderName)) {
                        chatExists.getSenderMessages().add(message);
                    } else {
                        chatExists.getAuthorAdminMessages().add(message);
                    }
                } else {
                    if (messagesList == null) {
                        messagesList = new ArrayList<>();
                    }
                    messagesList.add(message);
                    try {
                        Chat chat = new Chat((long) (chatList.size() + 1), authorAdmin, user, senderName, null,
                                messagesList);
                        chatList.add(chat);
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }

                JsonObject jsonMessage = new JsonObject();
                jsonMessage.addProperty("senderId", user.getUsername());
                jsonMessage.addProperty("text", message);
                String jsonString = jsonMessage.toString();

                // Broadcast the message to all connected clients
                for (WsContext conectedSession : userConnected) {
                    try {

                        if (!conectedSession.sessionId().equals(senderId)) {
                            System.out.println("jsonstring: " + jsonString);
                            conectedSession.send(jsonString);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            });

            ws.onClose(ctx -> {
                System.out.println("A user disconnected." + ctx.sessionId());
                userConnected.remove(ctx);
            });
        });
        
        app.post("/login", ApiController::login);

        app.before("/", ctx -> {

            User user = ctx.sessionAttribute("USER");

            if (user != null) {

                if (user.isAdmin()) {

                    ctx.redirect("/viewListArticle");
                } else if (user.isAuthor()) {

                    ctx.redirect("/viewListArticle");
                } else {

                    ctx.result("Error: Couldn't work out user");
                }
            } else {

                String username = LocalDateTime.now().toString();
                User tmp = new User("guess-" + username, "guess", null, false, false, null);
                ctx.sessionAttribute("USER", tmp);
                ctx.redirect("/viewListArticle");
            }

        });

        app.post("/closeSession", ctx -> {
            ctx.sessionAttribute("USER", null);
            ctx.redirect("/");
        });

        app.post("/verifyLogin", ctx -> {
            String username = ctx.formParam("username");
            // String password = ctx.formParam("password");

            try {

                User user = UserServices.getInstance().find(username);

                if (user != null) {

                    // password authentication
                    if (true/* user.getPassword().equals(password) */) {

                        ctx.sessionAttribute("USER", user);
                        if (user.isAdmin()) {

                            ctx.redirect("/viewListArticle");
                        } else if (user.isAuthor()) {

                            ctx.redirect("/viewListArticle");
                        } else {

                            ctx.redirect("/viewListArticle");
                        }
                    } else {

                        ctx.result("Error: Incorrect Password or Username");
                        TimeUnit.SECONDS.sleep(2);
                        // ctx.redirect("/");
                        ctx.redirect("/login.html");
                    }

                } else {

                    ctx.result("Error: Incorrect Password or Username");
                    TimeUnit.SECONDS.sleep(2);
                    // ctx.redirect("/");
                    ctx.redirect("/login.html");
                }

            } catch (Exception e) {

                System.out.println(e);
                e.printStackTrace();
                ctx.result("Error: " + e.getMessage());
            }

        });

        //for viewing
        app.get("/viewListUser", ctx -> {

            User user = ctx.sessionAttribute("USER");

            if (user == null) {
                ctx.redirect("/login.html");
                return;
            }

            if (!user.isAdmin()) {
                ctx.redirect("/viewListArticle");
            }

            int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
            int pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(5);

            List<User> users = UserServices.getInstance().findUsersWithPagination(page, pageSize);

            long totalUsers = UserServices.getInstance().countUsers();
            int totalPages = (int) Math.ceil((double) totalUsers / pageSize);

            Map<String, Object> model = new HashMap<>();
            model.put("USER", user.getUsername());
            model.put("userList", users);

            model.put("page", page);
            model.put("pageSize", pageSize);
            model.put("totalPages", totalPages);

            try {

                ctx.render("/public/viewUser.html", model);
            } catch (Exception e) {

                e.printStackTrace();
                ctx.result("Error: " + e.getMessage());
            }
        });

        app.get("/viewListArticle", ctx -> {

            User user = ctx.sessionAttribute("USER");

            int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
            int pageSize = ctx.queryParamAsClass("pageSize", Integer.class).getOrDefault(5);

            List<Article> articles = ArticleServices.getInstance().findArticlesWithPagination(page, pageSize);

            long totalArticles = ArticleServices.getInstance().countArticles();
            int totalPages = (int) Math.ceil((double) totalArticles / pageSize);

            Map<String, Object> model = new HashMap<>();

            model.put("user", "Invitado");
            model.put("isAdminView", false);
            model.put("isLogIn", false);

            if (user != null) {

                if (user.getPassword() != null) {
                    model.put("user", user.getUsername());
                    model.put("isLogIn", true);
                }
                if (user.isAuthor()) {
                    model.put("isAuthorView", true);
                }
                if (user.isAdmin()) {
                    model.put("isAdminView", true);
                }

            }

            model.put("articles", articles);
            model.put("page", page);
            model.put("pageSize", pageSize);
            model.put("totalPages", totalPages);

            try {

                ctx.render("/public/listArticle.html", model);
            } catch (Exception e) {

                e.printStackTrace();
                ctx.result("Error: " + e.getMessage());
            }
        });

        app.get("/viewListShortUrl", ctx -> {

            // int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
            // int pageSize = ctx.queryParamAsClass("pageSize",
            // Integer.class).getOrDefault(5);

            // List<Article> articles =
            // ArticleServices.getInstance().findArticlesWithPagination(page, pageSize);

            // long totalArticles = ArticleServices.getInstance().countArticles();
            // int totalPages = (int) Math.ceil((double) totalArticles / pageSize);

            User user = ctx.sessionAttribute("USER");
            List<ShortURL> shortUrlListActiveUser = new ArrayList<>();
            Map<String, Object> model = new HashMap<>();
            String clientDomain = ctx.host();

            System.out.println("session: " + ctx);
            model.put("user", "Invitado");
            model.put("domain", clientDomain);
            model.put("isLogIn", false);
            model.put("canDeleted", false);

            if (user != null) {

                if (user.getPassword() != null) {
                    model.put("user", user.getUsername());
                    model.put("isLogIn", true);
                }

                if (user.isAdmin()) {
                    shortUrlListActiveUser = mongoService.getAllShortURLs();
                    model.put("canDeleted", true);
                } else {
                    shortUrlListActiveUser = mongoService.getShortURLsByUsername(user.getUsername());
                }

                if (shortUrlListActiveUser != null) {

                    Collections.reverse(shortUrlListActiveUser);
                    model.put("urlList", shortUrlListActiveUser);
                }
            }

            model.put("urlList", shortUrlListActiveUser);

            // model.put("articles", articles);
            // model.put("page", page);
            // model.put("pageSize", pageSize);
            // model.put("totalPages", totalPages);

            try {

                ctx.render("/public/listShortUrl.html", model);
            } catch (Exception e) {

                e.printStackTrace();
                ctx.result("Error: " + e.getMessage());
            }
        });

        app.post("/viewShortUrlLogs", ctx -> {

            String clientDomain = ctx.host();
            List<URLAccessLog> logs = new ArrayList<>();
            Map<String, Object> model = new HashMap<>();

            String shortUrlString = ctx.formParam("shortUrlLetter");
            ShortURL url = mongoService.getShortURL(shortUrlString);

            logs = mongoService.getAccessLogsByUrlId(url.getId(), url).reversed();

            model.put("urlAccessLogs", logs);
            model.put("urlList", url.getShortUrl());
            model.put("domain", clientDomain);
            model.put("totalAmount", url.getAccessCount());

            try {
                ctx.render("/public/urlAccessLog.html", model);
            } catch (Exception e) {
                e.printStackTrace();
                ctx.result("Error: " + e.getMessage());
            }
        });

        app.post("/viewArticle", ctx -> {
            Long articleId;

            try {
                articleId = Long.valueOf(ctx.formParam("articleID"));
            } catch (NumberFormatException e) {
                ctx.result("Error: Invalid article ID");
                return;
            }

            Article article = ArticleServices.getInstance().find(articleId);
            if (article == null) {
                ctx.result("Error: Article not found");
                return;
            }

            // List<Comment> comments = (article.getComments() != null) ?
            // article.getComments() : new ArrayList<>();
            User user = ctx.sessionAttribute("USER");
            currentArticle = article;

            SimpleDateFormat dateArticleFormat = new SimpleDateFormat("dd-MM-yyyy");
            String dateArticleString = dateArticleFormat.format(article.getDate());

            Map<String, Object> model = new HashMap<>();
            model.put("article", article);
            model.put("user", user);
            model.put("dateString", dateArticleString);
            model.put("isView", true);

            // Debugging
            // System.out.println("Article: " + article);
            // System.out.println("User: " + user);
            // System.out.println("Date String: " + dateArticleString);

            try {
                ctx.render("/public/viewArticle.html", model);
            } catch (Exception e) {
                e.printStackTrace();
                ctx.result("Error: " + e.getMessage());
            }
        });

        //for creating
        app.post("/createUser", ctx -> {

            String name = ctx.formParam("name");
            String username = ctx.formParam("username");
            String password = ctx.formParam("password");
            boolean admin = "on".equals(ctx.formParam("admin"));
            boolean author = "on".equals(ctx.formParam("author"));

            // Manejar la subida de la foto
            UploadedFile image = ctx.uploadedFile("image");
            Image tmpImage = new Image();
            if (image != null) {
                tmpImage.setName(image.filename()); // Usar filename() para obtener el nombre del archivo
                tmpImage.setMimeType(image.contentType()); // Usar contentType() para obtener el tipo MIME

                // Leer el contenido del archivo y convertirlo a Base64
                InputStream inputStream = image.content(); // Obtener el InputStream
                byte[] bytes = inputStream.readAllBytes(); // Leer todos los bytes
                String fotoBase64 = Base64.getEncoder().encodeToString(bytes); // Convertir a Base64
                tmpImage.setFotoBase64(fotoBase64);
            }

            User user = new User(username, name, password, admin, author, tmpImage);

            try {

                UserServices.getInstance().create(user);
            } catch (Exception e) {

                e.printStackTrace();
                System.out.println(e);
                ctx.result("error" + e.getMessage());
            }

            User userLog = ctx.sessionAttribute("USER");
            if (userLog == null) {
                User tmp = UserServices.getInstance().find(username);
                ctx.sessionAttribute("USER", tmp);
                ctx.redirect("/viewListArticle");
            }

            ctx.redirect("/viewListUser");

        });

        app.post("/createArticle", ctx -> {
            String title = ctx.formParam("title");
            String body = ctx.formParam("body");
            // String tags = ctx.formParam("tags");
            User user = ctx.sessionAttribute("USER");

            try {

                Long nextId = ArticleServices.getInstance().getNextId();
                ArticleServices.getInstance().create(new Article(nextId, title, body, user, new Date(), null, null));

            } catch (Exception e) {

                e.printStackTrace();
                System.out.println(">" + e.getMessage());
            }

            ctx.redirect("/viewListArticle");
        });

        app.post("/createComment", ctx -> {
            String text = ctx.formParam("comment");
            Long articleId = Long.valueOf(ctx.formParam("articleID"));
            Article article = Head.getInstance().findArticle(articleId);
            User user = ctx.sessionAttribute("USER");

            Comment comment = new Comment(0, text, user, article, new Date());

            try {

                article.getComments().add(comment);
            } catch (Exception e) {

                e.printStackTrace();
                System.out.println(e);
                ctx.result("error" + e.getMessage());
            }

            Head.getInstance().updateArticle(article, articleId);
            Head.getInstance().add_comment(comment);

            // Redirect after the logic
            ctx.redirect("/viewArticle");
        });

        // to create a short Url
        app.post("/createShortUrl", ctx -> {

            User user = ctx.sessionAttribute("USER");
            String originalUrl = ctx.formParam("original");
            Map<String, Object> model = new HashMap<>();
            String errorMessage = "NONE";

            if (isValidURL(originalUrl)) {

                ShortURL url = new ShortURL();
                String clientDomain = ctx.host();
                long nextId = mongoService.getNextShortURLId();

                url.setId(nextId);
                url.setOriginalUrl(originalUrl);

                if (user != null) {
                    url.setUser(user);
                }

                String shortUrl = encodeUrl(nextId);
                url.setShortUrl(shortUrl);

                shortUrl = "http://" + clientDomain + "/url/" + shortUrl;
                model.put("shortUrl", shortUrl);
                model.put("originalUrl", originalUrl);
                model.put("isAfterGenerated", true);

                // creating inicial acces log of creation
                URLAccessLog creationLog = new URLAccessLog();
                creationLog.setId(mongoService.getNextAccessLogId());
                creationLog.setAccessTime(LocalDateTime.now());
                creationLog.setIp("creation");
                creationLog.setUrlEntry(url);
                url.getAccessLogs().add(creationLog);

                // urlList.add(url);

                try {
                    mongoService.saveShortURL(url);
                    mongoService.saveAccessLog(creationLog);

                } catch (Exception e) {
                    System.out.println("> :" + e.getMessage());
                    errorMessage = "couldnt save on mongoDB";
                    model.put("shortUrl", errorMessage);
                }

            } else {
                System.out.println("> is not valid url");
                errorMessage = "Invalid URL";
                model.put("shortUrl", errorMessage);
            }

            ctx.render("/public/createShortURL.html", model);

        });

        // to get the original url from the short url
        app.get("/url/{shortUrl}", ctx -> {

            String shortUrl = ctx.pathParam("shortUrl");
            System.out.println("enpoint functioning url: " + shortUrl);
            int index = 0;

            ShortURL url = mongoService.getShortURL(shortUrl);

            if (url == null) {
                ctx.status(404).result("Short URL not found");
                return;
            }

            mongoService.incrementAccessCount(url.getId());

            // Track visit
            String ip = ctx.ip();
            String browser = extractBrowser(ctx.userAgent());
            String clientDomain = ctx.host();
            String os = extractOS(ctx.header("User-Agent"));

            // Create an access log entry
            URLAccessLog log = new URLAccessLog();
            log.setId(mongoService.getNextAccessLogId());
            log.setUrlEntry(url);
            log.setIp(ip);
            log.setBrowser(browser);
            log.setClientDomain(clientDomain);
            log.setOs(os);
            log.setAccessTime(LocalDateTime.now());

            mongoService.saveAccessLog(log);

            ctx.redirect(url.getOriginalUrl());
        });

        //for editing
        app.post("/updateUserSpecific", ctx -> {

            User tmp = UserServices.getInstance().find(ctx.formParam("username"));

            Map<String, Object> model = new HashMap<>();
            model.put("user", tmp);
            model.put("isSignUp", false);

            User user = ctx.sessionAttribute("USER");
            if (user == null) {
                model.put("isSignUp", true);
            }

            try {

                ctx.render("/public/createUser.html", model);
            } catch (Exception e) {

                e.printStackTrace();
                System.out.println(e);
                ctx.result("error" + e.getMessage());
            }

        });

        app.post("/updateSpecificArticle", ctx -> {

            Article article = null;

            try {

                Long articleId = Long.parseLong(ctx.formParam("articleID"));
                article = ArticleServices.getInstance().find(articleId);
                // String tags = Head.getInstance().concatenatedTags(article.getTags());

            } catch (Exception e) {

                System.out.println("> " + e.getMessage());
            }

            Map<String, Object> model = new HashMap<>();

            model.put("article", article);
            // model.put("tagString", tags);

            try {

                ctx.render("/public/createArticle.html", model);
            } catch (Exception e) {

                e.printStackTrace();
                System.out.println(e);
                ctx.result("error" + e.getMessage());
            }

        });

        app.post("/editUser", ctx -> {

            User tmpUser = UserServices.getInstance().find(ctx.formParam("username"));
            String name = ctx.formParam("name");
            String username = ctx.formParam("username");
            String password = ctx.formParam("password");
            boolean admin = "on".equals(ctx.formParam("admin"));
            boolean author = "on".equals(ctx.formParam("author"));

            // Manejar la subida de la foto
            UploadedFile image = ctx.uploadedFile("image");
            Image tmpImage = new Image();
            if (image != null && image.size() > 0) {
                tmpImage.setName(image.filename());
                tmpImage.setMimeType(image.contentType());

                // Leer el contenido del archivo y convertirlo a Base64
                InputStream inputStream = image.content();
                byte[] bytes = inputStream.readAllBytes();
                String fotoBase64 = Base64.getEncoder().encodeToString(bytes);
                tmpImage.setFotoBase64(fotoBase64);
            } else {
                tmpImage = tmpUser.getProfilePicture();
            }

            User user = new User(username, name, password, admin, author, tmpImage);

            UserServices.getInstance().edit(user);

            ctx.redirect("/viewListUser");
        });

        app.post("/editArticle", ctx -> {
            Long articleId = Long.valueOf(ctx.formParam("articleID"));
            Article originalArticle = Head.getInstance().findArticle(articleId);

            String title = ctx.formParam("title");
            String body = ctx.formParam("body");
            String tags = ctx.formParam("tags");
            Date date = originalArticle.getDate();
            User user = originalArticle.getAuthor();

            Article article = new Article(articleId, title, body, user, date, originalArticle.getComments(),
                    Head.getInstance().separateTags(tags));

            Head.getInstance().updateArticle(article, articleId);

            // Redirect after the logic
            ctx.redirect("/viewListArticle");
        });

        //for deleting
        app.post("/deleteUser", ctx -> {
            User tmp = UserServices.getInstance().find(ctx.formParam("username"));
            boolean deleted = false;

            if (!tmp.getUsername().equals("admin")) {
                try {

                    deleted = UserServices.getInstance().delete(tmp.getUsername());
                } catch (Exception e) {

                    e.printStackTrace();
                    System.out.println(">" + e.getMessage());
                    ctx.result("error" + e.getMessage());
                }

            }

            if (deleted) {
                ctx.redirect("/viewListUser");
            } else {
                ctx.result("Error: No se pudo eliminar el usuario.");
            }
        });

        app.post("/deleteArticle", ctx -> {

            Article article = null;
            Long articleId = Long.parseLong(ctx.formParam("articleID"));

            if (articleId != null) {

                try {

                    article = ArticleServices.getInstance().find(articleId);

                } catch (Exception e) {

                    System.out.println("> " + e.getMessage());
                }
            }

            if (ArticleServices.getInstance().delete(articleId)) {
                ctx.redirect("/viewListArticle"); // Redirect after deleting
            } else {
                ctx.result("Error: No se pudo eliminar el article.");
            }
        });

        app.post("/deleteShortUrl", ctx -> {

            String ShortId = ctx.formParam("shortUrlId");
            User user = ctx.sessionAttribute("USER");

            try {
                Long id = Long.parseLong(ShortId);
                ShortURL url = mongoService.getShortURLById(id);

                if (url == null) {
                    ctx.status(404).result("Short URL not found");
                    return;
                }

                boolean deleted = mongoService.deleteShortURLById(id);
                
                if (deleted) {
                    ctx.redirect("/viewListShortUrl");
                } else {
                    ctx.status(500).result("Failed to delete short URL");
                }

            } catch (Exception e) {
                ctx.status(400).result("Invalid ID format, error: " + e.getMessage());
            }


        });

        // for testing mongoDB connection
        app.get("/mongo", ctx -> {

            MongoService mongo = new MongoService();
            mongo.testConnection();
        });
    }

    public static Chat doesChatExist(User author, User senderUser, String senderName) {
        Chat chatReturn = null;

        for (Chat chat : chatList) {
            if (chat.getAuthorAdmin().getUsername().equals(author.getUsername()) &&
                    chat.getSendUser().getUsername().equals(senderUser.getUsername()) ||
                    chat.getSenderName().equals(senderName)) {
                return chat;
            }
        }

        return chatReturn;
    }

    public static String encodeUrl(long num) {
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            sb.insert(0, BASE62.charAt((int) (num % 62)));
            num /= 62;
        }
        return sb.toString();
    }

    public static boolean isValidURL(String url) {

        try {

            new URL(url).toURI();
            return true;

        } catch (Exception e) {

            return false;
        }

    }

    public static String extractOS(String userAgent) {
        if (userAgent.contains("Windows"))
            return "Windows";
        if (userAgent.contains("Mac OS"))
            return "Mac OS";
        if (userAgent.contains("X11"))
            return "Unix";
        if (userAgent.contains("Android"))
            return "Android";
        if (userAgent.contains("Linux"))
            return "Linux";
        return "Unknown OS";
    }

    public static String extractBrowser(String userAgent) {
        if (userAgent.contains("Edg"))
            return "Microsoft Edge";
        if (userAgent.contains("Chrome") && userAgent.contains("Safari"))
            return "Google Chrome";
        if (userAgent.contains("Firefox"))
            return "Mozilla Firefox";
        if (userAgent.contains("Safari") && !userAgent.contains("Chrome"))
            return "Apple Safari";
        if (userAgent.contains("Opera") || userAgent.contains("OPR"))
            return "Opera";
        if (userAgent.contains("MSIE") || userAgent.contains("Trident"))
            return "Internet Explorer";
        return "Unknown Browser";
    }

    /**
     * Nos
     * 
     * @return
     */
    public static String getConnectionMethod() {
        return connectionMethod;
    }
}