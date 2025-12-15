package kr.yuns.springinitialize.common.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<GlobalResponse<?>> handleGlobalException(GlobalException e) {
        HttpStatus status = HttpStatus.valueOf(e.getErrorCode().getHttpStatus());

        return ResponseEntity
                .status(status)
                .body(GlobalResponse.error(e));
    }
}
