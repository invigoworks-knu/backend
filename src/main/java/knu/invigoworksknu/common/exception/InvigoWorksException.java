package knu.invigoworksknu.common.exception;

import knu.invigoworksknu.common.exception.errors.ErrorInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public class InvigoWorksException extends RuntimeException {

    private final ErrorInfo errorInfo;

    public HttpStatus status() {
        return errorInfo.status();
    }

    public String message() {
        return errorInfo.message();
    }
}
