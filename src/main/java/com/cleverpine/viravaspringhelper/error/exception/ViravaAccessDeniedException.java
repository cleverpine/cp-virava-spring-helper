package com.cleverpine.viravaspringhelper.error.exception;

import org.springframework.security.access.AccessDeniedException;

public class ViravaAccessDeniedException extends AccessDeniedException {

    public ViravaAccessDeniedException(String msg) {
        super(msg);
    }

    public ViravaAccessDeniedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
