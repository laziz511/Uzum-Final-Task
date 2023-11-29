package com.uzum.uzum_final_task.exception;

import java.util.NoSuchElementException;

public class AccountNotFoundException extends NoSuchElementException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
