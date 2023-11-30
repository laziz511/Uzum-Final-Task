package com.uzum.uzum_final_task.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CommissionNotFoundException extends RuntimeException {
    public CommissionNotFoundException(String message) {
        super(message);
    }
}

