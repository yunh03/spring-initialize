package kr.yuns.springinitialize.user.application;

import kr.yuns.springinitialize.user.domain.Email;
import kr.yuns.springinitialize.user.domain.UserRepository;
import kr.yuns.springinitialize.user.domain.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {
    private final UserRepository userRepository;

    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(Email.of(email))
                .orElseThrow(UserNotFoundException::new)
                .getId();
    }
}
