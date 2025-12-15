package kr.yuns.springinitialize.user.service;

import kr.yuns.springinitialize.common.response.GlobalResponse;
import kr.yuns.springinitialize.common.security.AuthenticationToken;
import kr.yuns.springinitialize.common.security.JwtTokenProvider;
import kr.yuns.springinitialize.user.data.dto.request.SignInRequestDto;
import kr.yuns.springinitialize.user.data.dto.request.SignUpRequestDto;
import kr.yuns.springinitialize.user.data.dto.response.TokenResponseDto;
import kr.yuns.springinitialize.user.data.entity.User;
import kr.yuns.springinitialize.user.data.exception.EmailDuplicationException;
import kr.yuns.springinitialize.user.data.exception.PasswordInvalidException;
import kr.yuns.springinitialize.user.data.exception.UserNotFoundException;
import kr.yuns.springinitialize.user.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public User getUserEntity(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException();
        } else {
            return user;
        }
    }

    private void validatePassword(String originalPassword, String password) {
        if(!passwordEncoder.matches(originalPassword, password)) {
            throw new PasswordInvalidException();
        }
    }

    public GlobalResponse<TokenResponseDto> signUp(SignUpRequestDto signUpRequestDto) {
        User user = User.builder()
                .email(signUpRequestDto.getEmail())
                .name(signUpRequestDto.getName())
                .password(passwordEncoder.encode(signUpRequestDto.getPassword()))
                .registeredAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            log.error("중복된 이메일 주소: {}", signUpRequestDto.getEmail());
            throw new EmailDuplicationException();
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null,
                asList(new SimpleGrantedAuthority(user.getRole().getValue())));
        AuthenticationToken authenticationToken = tokenProvider.generateToken(authentication);

        return GlobalResponse.ok(
                TokenResponseDto.builder()
                        .accessToken(authenticationToken.getAccessToken())
                        .refreshToken(authenticationToken.getRefreshToken())
                        .build());
    }

    public GlobalResponse<TokenResponseDto> signIn(SignInRequestDto signInRequestDto) {
        User user = getUserEntity(signInRequestDto.getEmail());
        validatePassword(signInRequestDto.getPassword(), user.getPassword());

        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null,
                asList(new SimpleGrantedAuthority(user.getRole().getValue())));
        AuthenticationToken authenticationToken = tokenProvider.generateToken(authentication);

        return GlobalResponse.ok(
                TokenResponseDto.builder()
                        .accessToken(authenticationToken.getAccessToken())
                        .refreshToken(authenticationToken.getRefreshToken())
                        .build());
    }

    private Collection<? extends GrantedAuthority> asList(SimpleGrantedAuthority simpleGrantedAuthority) {
        return Collections.singletonList(simpleGrantedAuthority);
    }
}
