package kr.yuns.springinitialize.user.application.dto;

public record SignUpCommand(String email, String name, String password) {
}
