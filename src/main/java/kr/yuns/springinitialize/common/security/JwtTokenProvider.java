package kr.yuns.springinitialize.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import kr.yuns.springinitialize.common.redis.RedisService;
import kr.yuns.springinitialize.user.data.enums.UserRole;
import kr.yuns.springinitialize.user.data.repository.UserRepository;
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
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtTokenProvider {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private static final String BLACKLIST_KEY_PREFIX = "blacklist:";
    private static final String BLACKLIST_VALUE = "logout";

    private final SecretKey key;
    private final RedisService redisService;
    private final UserRepository userRepository;

    // AT 만료 시간
    @Value("${jwt.access-token.expire-time}")
    private long ACCESS_TOKEN_EXPIRE_TIME;

    // RT 만료 시간
    @Value("${jwt.refresh-token.expire-time}")
    private long REFRESH_TOKEN_EXPIRE_TIME;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            RedisService redisService,
                            UserRepository userRepository) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.redisService = redisService;
        this.userRepository = userRepository;
    }

    private String createToken(String email, String authorities, Date expireDate) {
        log.info("[createToken] 새 JWT 발급 됨: {}", email);
        JwtBuilder builder = Jwts.builder()
                .subject(email)
                .expiration(expireDate)
                .signWith(key, Jwts.SIG.HS256);

        if (authorities != null && !authorities.isEmpty()) {
            builder.claim("authorities", authorities);
        }

        return builder.compact();
    }

    public AuthenticationToken generateToken(Authentication authentication) {
        String username = authentication.getName();

        log.info("[generateToken] 새 JWT 발급 시도: {}", username);

        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();

        String accessToken = createToken(username, authorities, new Date(now + ACCESS_TOKEN_EXPIRE_TIME));
        String refreshToken = createToken(username, null, new Date(now + REFRESH_TOKEN_EXPIRE_TIME));

        log.info("[generateToken] 발급된 Refresh Token이 Redis에 저장 됨");
        redisService.setValues(username, refreshToken, Duration.ofMillis(REFRESH_TOKEN_EXPIRE_TIME));

        return AuthenticationToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    public void invalidateToken(String accessToken) {
        Claims claims = parseClaims(accessToken);
        String username = claims.getSubject();

        redisService.deleteValues(username);
        log.info("[invalidateToken] 저장된 Refresh Token이 Redis에서 삭제 됨: {}", username);

        long remainingTime = claims.getExpiration().getTime() - System.currentTimeMillis();
        if (remainingTime <= 0) {
            log.info("[invalidateToken] 이미 만료된 Access Token이라 블랙리스트 등록 생략: {}", username);
            return;
        }

        redisService.setValues(BLACKLIST_KEY_PREFIX + accessToken, BLACKLIST_VALUE,
                Duration.ofMillis(remainingTime));
        log.info("[invalidateToken] Access Token이 블랙리스트에 등록 됨: {} (남은 시간: {}ms)", username, remainingTime);
    }

    public boolean isBlacklisted(String accessToken) {
        return redisService.hasKey(BLACKLIST_KEY_PREFIX + accessToken);
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);
        Object authClaim = claims.get("authorities");

        Collection<? extends GrantedAuthority> authorities;

        if (authClaim == null || authClaim.toString().isEmpty()) {
            String role = userRepository.findByEmail(claims.getSubject())
                    .map(user -> user.getRole().getValue())
                    .orElse(UserRole.USER.getValue());
            authorities = List.of(new SimpleGrantedAuthority(role));
        } else {
            authorities = Arrays.stream(authClaim.toString().split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }

        UserDetails principal = new org.springframework.security.core.userdetails.User(claims.getSubject(),
                "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
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

            if (isBlacklisted(token)) {
                log.warn("[validateToken] 로그아웃 처리된 JWT 인증 요청");
                return false;
            }

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
