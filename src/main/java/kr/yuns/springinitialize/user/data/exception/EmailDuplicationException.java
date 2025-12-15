package kr.yuns.springinitialize.user.data.exception;

import kr.yuns.springinitialize.common.response.ErrorCode;
import kr.yuns.springinitialize.common.response.GlobalException;

public class EmailDuplicationException extends GlobalException {
    public EmailDuplicationException() {
        super(ErrorCode.EMAIL_DUPLICATION);
    }
}
