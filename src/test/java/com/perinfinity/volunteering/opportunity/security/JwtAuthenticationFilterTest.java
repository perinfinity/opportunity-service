package com.perinfinity.volunteering.opportunity.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.Cookie;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String SECRET =
            Base64.getEncoder().encodeToString("test_secret_key_for_testing_only_32b".getBytes());

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "secretKey", SECRET);
    }

    // ─── Token extraction ────────────────────────────────────────────────────

    @Test
    void extractToken_returnsNull_whenNoTokenPresent() {
        assertThat(filter.extractToken(new MockHttpServletRequest())).isNull();
    }

    @Test
    void extractToken_readsFromAuthorizationHeader() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer some-token");
        assertThat(filter.extractToken(req)).isEqualTo("some-token");
    }

    @Test
    void extractToken_readsFromCookie_whenNoHeader() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie(JwtAuthenticationFilter.COOKIE_NAME, "cookie-token"));
        assertThat(filter.extractToken(req)).isEqualTo("cookie-token");
    }

    @Test
    void extractToken_prefersHeader_overCookie() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer header-token");
        req.setCookies(new Cookie(JwtAuthenticationFilter.COOKIE_NAME, "cookie-token"));
        assertThat(filter.extractToken(req)).isEqualTo("header-token");
    }

    @Test
    void extractToken_ignoresNonBearerHeader() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        assertThat(filter.extractToken(req)).isNull();
    }

    // ─── Full filter invocation ──────────────────────────────────────────────

    @Test
    void doFilter_setsAuthentication_whenValidToken() throws Exception {
        String token = buildToken("org@example.com", "ORGANIZATION", 1L);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer " + token);

        var response = new org.springframework.mock.web.MockHttpServletResponse();
        var chain = new org.springframework.mock.web.MockFilterChain();

        filter.doFilterInternal(req, response, chain);

        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo("org@example.com");
        assertThat(auth.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_ORGANIZATION"));
        assertThat(auth.getDetails()).isEqualTo(1L);

        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_doesNotSetAuthentication_whenTokenInvalid() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer invalid.token.value");

        var response = new org.springframework.mock.web.MockHttpServletResponse();
        var chain = new org.springframework.mock.web.MockFilterChain();

        filter.doFilterInternal(req, response, chain);

        assertThat(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication())
                .isNull();
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private String buildToken(String subject, String role, Long userId) {
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        return Jwts.builder()
                .setClaims(Map.of("role", role, "userId", userId))
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
