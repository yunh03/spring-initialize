package kr.yuns.springinitialize.common.security.exception;

import kr.yuns.springinitialize.common.response.ErrorCode;
import kr.yuns.springinitialize.common.response.GlobalException;

public class JwtAuthenticationException extends GlobalException {
    public JwtAuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }
}