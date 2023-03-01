package com.cleverpine.viravaspringhelper.error;

import java.time.LocalDateTime;

public record ViravaErrorResponse(LocalDateTime timestamp,
                                  Integer status,
                                  String error,
                                  String message,
                                  String path) {
}
