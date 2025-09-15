package com.sif.paywall.util;

import io.github.cdimascio.dotenv.Dotenv;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;


import java.security.Key;
import java.util.Date;

public class JwtUtil {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String SECRET_KEY = dotenv.get("JWT_SECRET");
    private static final long EXPIRATION_TIME = 86400000;
    private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
// 1 day in milliseconds

    public static String generateToken(String email,int userId) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        return Jwts.builder()
                .setSubject(email)
                .claim("userId",userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+EXPIRATION_TIME))
                .signWith(key,SignatureAlgorithm.HS256)
                .compact();
    }
    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Extract email
    public static String getEmailFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    public static int getUserIdFromToken(String token) {
        return (Integer) Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId");
    }
}


//  add to your Login flow
//String token = JwtUtil.generateToken("samuel");
//System.out.println("JWT: " + token);