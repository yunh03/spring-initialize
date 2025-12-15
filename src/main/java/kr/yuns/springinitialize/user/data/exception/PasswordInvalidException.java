package kr.yuns.springinitialize.user.data.exception;

import kr.yuns.springinitialize.common.response.ErrorCode;
import kr.yuns.springinitialize.common.response.GlobalException;

public class PasswordInvalidException extends GlobalException {
    public PasswordInvalidException() {
        super(ErrorCode.PASSWORD_INVALID);
    }
}