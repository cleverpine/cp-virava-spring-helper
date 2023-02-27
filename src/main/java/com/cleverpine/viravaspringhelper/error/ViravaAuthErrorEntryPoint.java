package com.cleverpine.viravaspringhelper.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.time.LocalDateTime;

public class ViravaAuthErrorEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public ViravaAuthErrorEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        var unauthorizedHttpStatus = HttpStatus.UNAUTHORIZED;

        response.setStatus(unauthorizedHttpStatus.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        var errorResponse = new ViravaErrorResponse(
                LocalDateTime.now(),
                HttpServletResponse.SC_UNAUTHORIZED,
                unauthorizedHttpStatus.getReasonPhrase(),
                authException.getMessage(),
                request.getRequestURI());

        response.getWriter()
                .write(objectMapper.writeValueAsString(errorResponse));
    }

}
