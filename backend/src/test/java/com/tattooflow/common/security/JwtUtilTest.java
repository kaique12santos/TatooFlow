package com.tattooflow.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static final String EMAIL = "artista@tattooflow.com";
    private JwtUtil jwtUtil;
    private Key key;

    @BeforeEach
    void preparar() {
        jwtUtil = new JwtUtil();
        // Usa a chave real da instancia para criar tokens expirados sem esperas.
        key = (Key) ReflectionTestUtils.getField(jwtUtil, "key");
    }

    @Test
    void deveGerarTokenAssinadoComUsuarioEValidadeDe24Horas() {
        long antes = System.currentTimeMillis();
        String token = jwtUtil.generateToken(EMAIL);
        long depois = System.currentTimeMillis();

        var jwt = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        Claims claims = jwt.getBody();

        assertEquals("HS256", jwt.getHeader().getAlgorithm());
        assertEquals(EMAIL, claims.getSubject());
        // JWT serializa datas com precisao de segundos.
        assertTrue(claims.getIssuedAt().getTime() >= antes - 999);
        assertTrue(claims.getIssuedAt().getTime() <= depois);
        assertTrue(claims.getExpiration().getTime() >= antes + 86_400_000 - 999);
        assertTrue(claims.getExpiration().getTime() <= depois + 86_400_000);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals(EMAIL, jwtUtil.getUsernameFromToken(token));
    }

    @Test
    void deveRejeitarTokenExpiradoMesmoComAssinaturaValida() {
        String token = Jwts.builder().setSubject(EMAIL)
                .setIssuedAt(new Date(System.currentTimeMillis() - 120_000))
                .setExpiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(key).compact();

        assertFalse(jwtUtil.validateToken(token));
        assertThrows(ExpiredJwtException.class, () -> jwtUtil.getUsernameFromToken(token));
    }

    @Test
    void deveRejeitarTokenAssinadoComOutraChave() {
        String token = Jwts.builder().setSubject(EMAIL)
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.secretKeyFor(SignatureAlgorithm.HS256)).compact();

        assertFalse(jwtUtil.validateToken(token));
    }

    @Test
    void deveRejeitarPayloadAdulterado() {
        String[] partes = jwtUtil.generateToken(EMAIL).split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8);
        partes[1] = Base64.getUrlEncoder().withoutPadding().encodeToString(
                payload.replace(EMAIL, "admin@tattooflow.com").getBytes(StandardCharsets.UTF_8));

        assertFalse(jwtUtil.validateToken(String.join(".", partes)));
    }

    @Test
    void deveRejeitarTokenSemAssinatura() {
        String token = Jwts.builder().setSubject(EMAIL)
                .setExpiration(new Date(System.currentTimeMillis() + 60_000)).compact();

        assertFalse(jwtUtil.validateToken(token));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "token-invalido", "abc.def.ghi"})
    void deveRejeitarTokenAusenteOuMalformado(String token) {
        assertFalse(jwtUtil.validateToken(token));
    }
}
