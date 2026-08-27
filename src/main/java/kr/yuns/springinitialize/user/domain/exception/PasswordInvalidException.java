package kr.yuns.springinitialize.user.domain.exception;

import kr.yuns.springinitialize.global.response.ErrorCode;
import kr.yuns.springinitialize.global.response.GlobalException;

public class PasswordInvalidException extends GlobalException {
    public PasswordInvalidException() {
        super(ErrorCode.PASSWORD_INVALID);
    }
}
