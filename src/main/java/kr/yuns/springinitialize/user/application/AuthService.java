package kr.yuns.springinitialize.user.application;

import kr.yuns.springinitialize.user.application.dto.SignInCommand;
import kr.yuns.springinitialize.user.application.dto.SignUpCommand;
import kr.yuns.springinitialize.user.application.dto.TokenResult;
import kr.yuns.springinitialize.user.domain.Email;
import kr.yuns.springinitialize.user.domain.User;
import kr.yuns.springinitialize.user.domain.UserRepository;
import kr.yuns.springinitialize.user.domain.exception.EmailDuplicationException;
import kr.yuns.springinitialize.user.domain.exception.UserNotFoundException;
import kr.yuns.springinitialize.user.infrastructure.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public TokenResult signUp(SignUpCommand command) {
        Email email = Email.of(command.email());

        if (userRepository.existsByEmail(email)) {
            log.error("[signUp] 중복된 이메일 주소로 인한 가입 거부: {}", email);
            throw new EmailDuplicationException();
        }

        User user;
        try {
            user = userRepository.saveAndFlush(
                    User.register(email, command.name(), command.password(), passwordEncoder)
            );
        } catch (DataIntegrityViolationException e) {
            log.error("[signUp] 동시 가입 경합으로 인한 이메일 중복: {}", email);
            throw new EmailDuplicationException();
        }
        log.info("[signUp] 새로운 사용자 등록: {}", user.getEmailValue());

        return tokenProvider.issue(user);
    }

    @Transactional(readOnly = true)
    public TokenResult signIn(SignInCommand command) {
        User user = getByEmail(Email.of(command.email()));
        user.verifyPassword(command.password(), passwordEncoder);

        return tokenProvider.issue(user);
    }

    private User getByEmail(Email email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("[getByEmail] 사용자 조회 실패: {}", email);
                    return new UserNotFoundException();
                });
    }
}
