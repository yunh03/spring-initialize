package kr.yuns.springinitialize.common.security.exception;

import kr.yuns.springinitialize.common.response.ErrorCode;
import kr.yuns.springinitialize.common.response.GlobalException;

public class TokenInvalidException extends GlobalException {
    public TokenInvalidException() {
        super(ErrorCode.TOKEN_INVALID);
    }
}
