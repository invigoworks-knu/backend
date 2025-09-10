package knu.invigoworksknu.common.exception;

import knu.invigoworksknu.common.domain.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvigoWorksException.class)
    public ApiResponse<String> handleInvigoWorksException(InvigoWorksException e) {
        return ApiResponse.of(e.status(), e.message());
    }
}
