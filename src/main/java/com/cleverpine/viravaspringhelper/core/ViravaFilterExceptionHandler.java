package com.cleverpine.viravaspringhelper.core;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@FunctionalInterface
public interface ViravaFilterExceptionHandler {
    void handle(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException;
}
