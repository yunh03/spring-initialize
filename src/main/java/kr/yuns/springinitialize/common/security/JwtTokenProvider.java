package kr.yuns.springinitialize.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import kr.yuns.springinitialize.common.response.ErrorCode;
import kr.yuns.springinitialize.common.redis.RedisService;
import kr.yuns.springinitialize.common.security.exception.JwtAuthenticationException;
import kr.yuns.springinitialize.user.data.entity.User;
import kr.yuns.springinitialize.user.data.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtTokenProvider {
    private final Key key;
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
                .setSubject(email)
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256);

        if(authorities != null && !authorities.isEmpty()) {
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

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);
        Object authClaim = claims.get("authorities");

        Collection<? extends GrantedAuthority> authorities;

        if (authClaim == null || authClaim.toString().isEmpty()) {
            User user = userRepository.findByEmail(claims.getSubject());
            String role = (user != null) ? user.getRole().getValue() : "ROLE_USER";
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
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("[validateToken] 유효하지 않은 JWT 인증 요청: {}", e.getMessage());
            throw new JwtAuthenticationException(ErrorCode.TOKEN_INVALID);
        } catch (ExpiredJwtException e) {
            log.warn("[validateToken] 만료된 JWT 인증 요청: {}", e.getMessage());
            throw new JwtAuthenticationException(ErrorCode.TOKEN_INVALID);
        } catch (UnsupportedJwtException e) {
            log.warn("[validateToken] 지원되지 않는 JWT 인증 요청: {}", e.getMessage());
            throw new JwtAuthenticationException(ErrorCode.TOKEN_INVALID);
        } catch (IllegalArgumentException e) {
            log.warn("[validateToken] JWT가 제출되지 않음: {}", e.getMessage());
            throw new JwtAuthenticationException(ErrorCode.TOKEN_INVALID);
        }
    }
}
