package practica.logic;

import io.jsonwebtoken.*;
import java.util.Date;

public class JWTUtil {
    // private static final String SECRET_KEY = "yourSecretKey123"; // Use env var or config in production

    // public static String generateToken(User user) {
    //     return Jwts.builder()
    //         .setSubject(user.getUsername())
    //         .claim("admin", user.isAdmin())
    //         .setIssuedAt(new Date())
    //         .setExpiration(new Date(System.currentTimeMillis() + 3600_000)) // 1 hour expiry
    //         .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
    //         .compact();
    // }

    // public static Jws<Claims> validateToken(String token) throws JwtException {
    //     return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
    // }
}
