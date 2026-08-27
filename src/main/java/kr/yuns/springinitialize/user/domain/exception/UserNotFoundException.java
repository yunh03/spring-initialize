package kr.yuns.springinitialize.user.domain.exception;

import kr.yuns.springinitialize.global.response.ErrorCode;
import kr.yuns.springinitialize.global.response.GlobalException;

public class UserNotFoundException extends GlobalException {
    public UserNotFoundException() {
        super(ErrorCode.USER_DATA_NOT_FOUND);
    }
}
