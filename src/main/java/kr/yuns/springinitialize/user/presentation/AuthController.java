package kr.yuns.springinitialize.user.presentation;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import kr.yuns.springinitialize.global.response.GlobalResponse;
import kr.yuns.springinitialize.user.application.AuthService;
import kr.yuns.springinitialize.user.presentation.dto.SignInRequest;
import kr.yuns.springinitialize.user.presentation.dto.SignUpRequest;
import kr.yuns.springinitialize.user.presentation.dto.TokenResponse;
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
    @Operation(summary = "사용자 회원가입")
    public GlobalResponse<TokenResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        return GlobalResponse.ok(TokenResponse.from(authService.signUp(request.toCommand())));
    }

    @PostMapping("/signin")
    @Operation(summary = "사용자 로그인")
    public GlobalResponse<TokenResponse> signIn(@Valid @RequestBody SignInRequest request) {
        return GlobalResponse.ok(TokenResponse.from(authService.signIn(request.toCommand())));
    }
}
