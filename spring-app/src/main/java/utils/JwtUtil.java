package utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtUtil {
    private String secret = "c29tZS1zZWNyZXQtcGhyYXNl"; // TODO: to config

    public String parseToken(String token) {
        try {
            Claims body = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();

            return body.getSubject();

        } catch (JwtException | ClassCastException | IllegalArgumentException e) {
            return null;
        }
    }

    public String generateToken(String chat_id) {
        Claims claims = Jwts.claims().setSubject(chat_id);

        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
}
