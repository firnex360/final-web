package practica.logic;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.security.Keys;
import practica.services.UserServices;

public class ApiController {

    private static MongoService mongoService = new MongoService();
    private static final String BASE62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static final String JWT_SECRET = "my_super_secret_key_which_is_32chars!";


    @OpenApi(path = "/api/user/{username}", methods = HttpMethod.GET, pathParams = {
            @OpenApiParam(name = "username", description = "Username of the user")
    }, responses = {
            @OpenApiResponse(status = "200", description = "List of URLs with statistics", content = {
                    @OpenApiContent(from = ShortURL.class) })
    })
    public static void getUrlsByUser(Context ctx) throws Exception {
        String username = ctx.pathParam("username");
        List<ShortURL> urls = mongoService.getShortURLsByUsername(username);
        List<ShortURL> result = new ArrayList<>();

        for (ShortURL shortURL : urls) {
            List<URLAccessLog> logs = mongoService.getAccessLogsByUrlId(shortURL.getId(), shortURL);
            shortURL.setAccessLogs(logs);
            result.add(shortURL);
        }

        try {

            ctx.json(result);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error >>>: " + e.getMessage());
        }
    }

    @OpenApi(path = "/api/user/{username}", methods = HttpMethod.POST, pathParams = {
        @OpenApiParam(name = "username", description = "Username of the user")
}, queryParams = {
        @OpenApiParam(name = "original", description = "URL to be shorten")
}, responses = {
        @OpenApiResponse(status = "200", description = "Shortened URL with preview", content = {
                @OpenApiContent(from = ShortURLResponse.class) })
})
    public static void createShortUrlAPI(Context ctx) throws Exception {
        String username = ctx.pathParam("username");
        String originalUrl = ctx.formParam("original");

        if (!isValidURL(originalUrl)) {
            ctx.status(400).json(Map.of("error", "Invalid URL"));
            return;
        }

        long nextId = mongoService.getNextShortURLId();
        String encodedShort = encodeUrl(nextId);
        String clientDomain = ctx.host();
        String fullShortUrl = "http://" + clientDomain + "/url/" + encodedShort;

        User user = new User();
        user = UserServices.getInstance().findByUsername(username);

        if (user == null) {
            ctx.status(404).json(Map.of("error", "User not found"));
            return;
        }

        ShortURL url = new ShortURL();
        url.setId(nextId);
        url.setOriginalUrl(originalUrl);
        url.setShortUrl(encodedShort);
        url.setUser(user);

        URLAccessLog creationLog = new URLAccessLog();
        creationLog.setId(mongoService.getNextAccessLogId());
        creationLog.setAccessTime(LocalDateTime.now());
        creationLog.setIp("creation");
        creationLog.setUrlEntry(url);
        url.getAccessLogs().add(creationLog);

        mongoService.saveShortURL(url);
        mongoService.saveAccessLog(creationLog);

        String base64Preview = ScreenshotUtils.generateBase64Preview(originalUrl);

        ShortURLResponse response = new ShortURLResponse(
            originalUrl,
            fullShortUrl,
            creationLog.getAccessTime().toString(),
            base64Preview
        );
        ctx.json(response);
    }

    public static void login(Context ctx) {
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");

        User user = UserServices.getInstance().findByUsername(username);

        if (user == null || !user.getPassword().equals(password)) {
            ctx.status(401).result("Invalid username or password");
            return;
        }

        SecretKey secretKey = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));

        String jwt = Jwts.builder()
                .setIssuer("ShortURL-Service")
                .setSubject(user.getUsername())
                .claim("admin", user.isAdmin())
                .setExpiration(Date.from(LocalDateTime.now().plusMinutes(15).toInstant(ZoneOffset.ofHours(-4))))
                .signWith(secretKey)
                .compact();

        ctx.json(Map.of("token", jwt));
    }

    public static void jwtFilter(Context ctx) {
        String authHeader = ctx.header("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedResponse("Missing or invalid Authorization header");
        }

        String token = authHeader.replace("Bearer ", "");

        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            ctx.attribute("user", claims.getSubject()); // set username in context
            ctx.attribute("admin", claims.get("admin", Boolean.class)); // if needed

        } catch (ExpiredJwtException | MalformedJwtException | SignatureException e) {
            throw new ForbiddenResponse("Invalid or expired token");
        }
    }



    public record ShortURLResponse(
        String originalUrl,
        String shortUrl,
        String createdAt,
        String previewImageBase64
    ) {}

    public static boolean isValidURL(String url) {

        try {

            new URL(url).toURI();
            return true;

        } catch (Exception e) {

            return false;
        }

    }

    public static String encodeUrl(long num) {
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            sb.insert(0, BASE62.charAt((int) (num % 62)));
            num /= 62;
        }
        return sb.toString();
    }

}
