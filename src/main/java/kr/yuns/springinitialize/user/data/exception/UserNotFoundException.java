package kr.yuns.springinitialize.user.data.exception;

import kr.yuns.springinitialize.common.response.ErrorCode;
import kr.yuns.springinitialize.common.response.GlobalException;

public class UserNotFoundException extends GlobalException {
    public UserNotFoundException() {
        super(ErrorCode.USER_DATA_NOT_FOUND);
    }
}