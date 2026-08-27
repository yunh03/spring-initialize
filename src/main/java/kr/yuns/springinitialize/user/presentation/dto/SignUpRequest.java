package kr.yuns.springinitialize.user.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kr.yuns.springinitialize.user.application.dto.SignUpCommand;

public record SignUpRequest(
        @NotBlank @Email String email,
        @NotBlank String name,
        @NotBlank @Size(min = 8, max = 64) String password
) {
    public SignUpCommand toCommand() {
        return new SignUpCommand(email, name, password);
    }
}
