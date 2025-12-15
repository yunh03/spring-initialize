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
    private long accessTokenExpireTime;

    // RT 만료 시간
    @Value("${jwt.refresh-token.expire-time}")
    private long refreshTokenExpireTime;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            RedisService redisService,
                            UserRepository userRepository) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.redisService = redisService;
        this.userRepository = userRepository;
    }

    private String generateToken(String username, String authorities, Date expireDate) {
        return Jwts.builder()
                .setSubject(username)
                .claim("auth", authorities)
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public AuthenticationToken generateToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        String username = authentication.getName();

        Date accessTokenExpire = new Date(now + accessTokenExpireTime);
        String accessToken = generateToken(username, authorities, accessTokenExpire);

        Date refreshTokenExpire = new Date(now + refreshTokenExpireTime);
        String refreshToken = generateToken(username, authorities, refreshTokenExpire);

        redisService.setValues(username, refreshToken, Duration.ofMillis(refreshTokenExpireTime));

        log.info("신규 JWT 생성: {}", username);
        return AuthenticationToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);

        Collection<? extends GrantedAuthority> authorities;
        if (claims.get("auth") == null || claims.get("auth").toString().isEmpty()) {
            User user = userRepository.findByEmail(claims.getSubject());
            if (user != null) {
                authorities = List.of(new SimpleGrantedAuthority(user.getRole().getValue()));
            } else {
                authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            }
        } else {
            authorities = Arrays.stream(claims.get("auth").toString().split(","))
                    .filter(auth -> !auth.trim().isEmpty())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            if (authorities.isEmpty()) {
                User user = userRepository.findByEmail(claims.getSubject());
                if (user != null) {
                    authorities = List.of(new SimpleGrantedAuthority(user.getRole().getValue()));
                } else {
                    authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                }
            }
        }

        UserDetails principal = new org.springframework.security.core.userdetails.User(claims.getSubject(), "", authorities);
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
            log.warn("유효하지 않은 JWT", e);
            throw new JwtAuthenticationException(ErrorCode.TOKEN_INVALID);
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT", e);
            throw new JwtAuthenticationException(ErrorCode.TOKEN_INVALID);
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT", e);
            throw new JwtAuthenticationException(ErrorCode.TOKEN_INVALID);
        } catch (IllegalArgumentException e) {
            log.warn("JWT가 제출되지 않음", e);
            throw new JwtAuthenticationException(ErrorCode.TOKEN_INVALID);
        }
    }
}
