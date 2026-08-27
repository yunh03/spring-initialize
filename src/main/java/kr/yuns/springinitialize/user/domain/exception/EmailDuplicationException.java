package kr.yuns.springinitialize.user.domain.exception;

import kr.yuns.springinitialize.global.response.ErrorCode;
import kr.yuns.springinitialize.global.response.GlobalException;

public class EmailDuplicationException extends GlobalException {
    public EmailDuplicationException() {
        super(ErrorCode.EMAIL_DUPLICATION);
    }
}
