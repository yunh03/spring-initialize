package kr.yuns.springinitialize.product.domain.exception;

import kr.yuns.springinitialize.global.response.ErrorCode;
import kr.yuns.springinitialize.global.response.GlobalException;

public class ProductNotFoundException extends GlobalException {
    public ProductNotFoundException() {
        super(ErrorCode.DATA_NOT_FOUND);
    }
}
