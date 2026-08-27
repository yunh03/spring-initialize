package kr.yuns.springinitialize.global.security.exception;

public class AuthenticationInfoException extends RuntimeException {
    public AuthenticationInfoException() {
        super("사용자 정보를 찾을 수 없습니다.");
    }
}
