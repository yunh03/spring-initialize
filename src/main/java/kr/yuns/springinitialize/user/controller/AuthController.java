package kr.yuns.springinitialize.user.controller;

import kr.yuns.springinitialize.common.response.GlobalResponse;
import kr.yuns.springinitialize.user.data.dto.request.SignInRequestDto;
import kr.yuns.springinitialize.user.data.dto.request.SignUpRequestDto;
import kr.yuns.springinitialize.user.data.dto.response.TokenResponseDto;
import kr.yuns.springinitialize.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public GlobalResponse<TokenResponseDto> signUp(@RequestBody SignUpRequestDto signUpRequestDto) {
        return authService.signUp(signUpRequestDto);
    }

    @PostMapping("/signin")
    public GlobalResponse<TokenResponseDto> signIn(@RequestBody SignInRequestDto signInRequestDto) {
        return authService.signIn(signInRequestDto);
    }
}
