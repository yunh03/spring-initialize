package kr.yuns.springinitialize.user.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import kr.yuns.springinitialize.user.application.dto.SignInCommand;

public record SignInRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
    public SignInCommand toCommand() {
        return new SignInCommand(email, password);
    }
}
