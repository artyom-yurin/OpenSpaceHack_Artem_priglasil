package utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import models.ConversationData;
import models.ConversationState;

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

    public String generateToken(ConversationData cd) {
        Claims claims = Jwts.claims().setSubject(cd.getChatId());

        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
}