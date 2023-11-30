package com.uzum.uzum_final_task.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class SecretKeyMismatchException extends RuntimeException {
    public SecretKeyMismatchException(String message) {
        super(message);
    }
}
