package com.cleverpine.viravaspringhelper.error.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidTokenAuthenticationException extends AuthenticationException {

    public InvalidTokenAuthenticationException(String message) {
        super(message);
    }

    public InvalidTokenAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
