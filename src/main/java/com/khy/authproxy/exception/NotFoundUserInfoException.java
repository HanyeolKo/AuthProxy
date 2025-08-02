package com.khy.authproxy.exception;

public class NotFoundUserInfoException extends RuntimeException {
    public NotFoundUserInfoException(String message) {
        super(message);
    }
}
