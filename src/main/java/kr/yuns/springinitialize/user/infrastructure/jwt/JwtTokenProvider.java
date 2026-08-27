package kr.yuns.springinitialize.user.infrastructure.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import kr.yuns.springinitialize.global.redis.RedisService;
import kr.yuns.springinitialize.user.application.dto.TokenResult;
import kr.yuns.springinitialize.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtTokenProvider {
    private static final String AUTHORITIES_CLAIM = "authorities";

    private final SecretKey key;
    private final RedisService redisService;

    @Value("${jwt.access-token.expire-time}")
    private long ACCESS_TOKEN_EXPIRE_TIME;

    @Value("${jwt.refresh-token.expire-time}")
    private long REFRESH_TOKEN_EXPIRE_TIME;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey, RedisService redisService) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.redisService = redisService;
    }

    public TokenResult issue(User user) {
        String email = user.getEmailValue();
        log.info("[issue] 새 JWT 발급 시도: {}", email);

        long now = (new Date()).getTime();

        String accessToken = createToken(email, user.getRoleValue(), new Date(now + ACCESS_TOKEN_EXPIRE_TIME));
        String refreshToken = createToken(email, null, new Date(now + REFRESH_TOKEN_EXPIRE_TIME));

        log.info("[issue] 발급된 Refresh Token이 Redis에 저장 됨");
        redisService.setValues(email, refreshToken, Duration.ofMillis(REFRESH_TOKEN_EXPIRE_TIME));

        return new TokenResult(accessToken, refreshToken);
    }

    private String createToken(String email, String authorities, Date expireDate) {
        log.info("[createToken] 새 JWT 발급 됨: {}", email);
        JwtBuilder builder = Jwts.builder()
                .subject(email)
                .expiration(expireDate)
                .signWith(key, Jwts.SIG.HS256);

        if (authorities != null && !authorities.isEmpty()) {
            builder.claim(AUTHORITIES_CLAIM, authorities);
        }

        return builder.compact();
    }

    public Optional<Authentication> getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);
        Object authClaim = claims.get(AUTHORITIES_CLAIM);

        if (authClaim == null || authClaim.toString().isEmpty()) {
            log.warn("[getAuthentication] 권한 정보가 없는 토큰으로 인증 시도: {}", claims.getSubject());
            return Optional.empty();
        }

        Collection<? extends GrantedAuthority> authorities = Arrays.stream(authClaim.toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        UserDetails principal = new org.springframework.security.core.userdetails.User(claims.getSubject(),
                "", authorities);
        return Optional.of(new UsernamePasswordAuthenticationToken(principal, "", authorities));
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException | MalformedJwtException e) {
            log.warn("[validateToken] 유효하지 않은 JWT 서명 요청: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("[validateToken] 만료된 JWT 인증 요청: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("[validateToken] 지원되지 않는 JWT 인증 요청: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("[validateToken] JWT가 제출되지 않음: {}", e.getMessage());
        }
        return false;
    }
}
