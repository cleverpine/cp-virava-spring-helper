package com.cleverpine.viravaspringhelper.error.exception;

import org.springframework.security.core.AuthenticationException;

public class ViravaAuthenticationException extends AuthenticationException {

    public ViravaAuthenticationException(String message) {
        super(message);
    }

    public ViravaAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
