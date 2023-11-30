package com.uzum.uzum_final_task.exception;

import com.uzum.uzum_final_task.utils.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            SecretKeyMismatchException.class,
            CommissionNotFoundException.class,
            AccountNotFoundException.class,
            NotEnoughMoneyException.class,
            OfficialRateFetchException.class
    })
    public ResponseEntity<ApiErrorResponse> handleApiException(RuntimeException ex) {
        HttpStatus status = resolveHttpStatus(ex);
        ApiErrorResponse apiError = new ApiErrorResponse(ex.getMessage(), status.value(), status, LocalDateTime.now());
        return ResponseEntity.status(status).body(apiError);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleException() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
    }

    private HttpStatus resolveHttpStatus(Exception ex) {
        ResponseStatus responseStatus = ex.getClass().getAnnotation(ResponseStatus.class);
        return (responseStatus != null) ? responseStatus.value() : HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
