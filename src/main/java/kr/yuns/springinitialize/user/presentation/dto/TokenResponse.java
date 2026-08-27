package kr.yuns.springinitialize.user.presentation.dto;

import kr.yuns.springinitialize.user.application.dto.TokenResult;

public record TokenResponse(String accessToken, String refreshToken) {
    public static TokenResponse from(TokenResult result) {
        return new TokenResponse(result.accessToken(), result.refreshToken());
    }
}
