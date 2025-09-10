package knu.invigoworksknu.common.exception.errors;

import org.springframework.http.HttpStatus;

public interface ErrorInfo {

    HttpStatus status();

    String message();
}
