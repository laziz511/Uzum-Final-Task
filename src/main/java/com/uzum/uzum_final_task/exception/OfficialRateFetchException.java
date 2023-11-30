package com.uzum.uzum_final_task.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class OfficialRateFetchException extends RuntimeException {
    public OfficialRateFetchException(String message) {
        super(message);
    }
}
