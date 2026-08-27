package kr.yuns.springinitialize.user.domain.exception;

import kr.yuns.springinitialize.global.response.ErrorCode;
import kr.yuns.springinitialize.global.response.GlobalException;

public class EmailInvalidException extends GlobalException {
    public EmailInvalidException() {
        super(ErrorCode.INVALID_PARAMETER);
    }
}
