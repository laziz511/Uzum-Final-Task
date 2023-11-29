package com.uzum.uzum_final_task.exception;

import java.util.NoSuchElementException;

public class CommissionNotFoundException extends NoSuchElementException {
    public CommissionNotFoundException(String message) {
        super(message);
    }
}

