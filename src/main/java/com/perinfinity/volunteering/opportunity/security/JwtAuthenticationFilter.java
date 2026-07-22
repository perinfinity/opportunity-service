package com.perinfinity.volunteering.opportunity.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.List;

/**
 * Validates the JWT from (in order):
 *  1. Authorization: Bearer header — API / mobile clients
 *  2. auth_token httpOnly cookie  — browser clients
 *
 * On success, sets a UsernamePasswordAuthenticationToken in SecurityContext
 * with the user email as principal and ROLE_<ROLE> as authority.
 * A "userId" detail is attached for downstream ownership checks.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    static final String COOKIE_NAME = "auth_token";

    @Value("${jwt.secret.key}")
    private String secretKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (token != null) {
            try {
                Claims claims = parseToken(token);
                String email = claims.getSubject();
                String role = claims.get("role", String.class);
                // JJWT may deserialise small longs as Integer — handle both
                Number userIdNum = (Number) claims.get("userId");
                Long userId = userIdNum != null ? userIdNum.longValue() : null;

                if (email != null && role != null
                        && SecurityContextHolder.getContext().getAuthentication() == null) {

                    var auth = new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                    auth.setDetails(userId);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (JwtException ignored) {
                // Invalid token — request proceeds as unauthenticated;
                // security rules will reject it if the endpoint requires auth.
            }
        }
        filterChain.doFilter(request, response);
    }

    String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (COOKIE_NAME.equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }

    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] key = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(key);
    }
}
