package kr.yuns.springinitialize.product.data.exception;

import kr.yuns.springinitialize.common.response.ErrorCode;
import kr.yuns.springinitialize.common.response.GlobalException;

public class ProductNotFoundException extends GlobalException {
    public ProductNotFoundException() {
        super(ErrorCode.DATA_NOT_FOUND);
    }
}