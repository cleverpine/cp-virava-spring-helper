package com.cleverpine.viravaspringhelper.error.exception;

public class ViravaAuthenticationException extends RuntimeException {

    public ViravaAuthenticationException(String message) {
        super(message);
    }

    public ViravaAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
