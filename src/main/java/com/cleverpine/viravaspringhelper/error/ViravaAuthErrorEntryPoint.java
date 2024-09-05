package com.cleverpine.viravaspringhelper.error;

import com.cleverpine.viravaspringhelper.core.ViravaFilterExceptionHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.function.Supplier;

public class ViravaAuthErrorEntryPoint implements AuthenticationEntryPoint {

    private ObjectMapper objectMapper;
    private ViravaFilterExceptionHandler viravaFilterExceptionHandler;

    /**
     * Creates a new instance of {@link ViravaAuthErrorEntryPoint} with the given {@link ObjectMapper}.
     * Use this constructor if you want the default error response to be generated.
     *
     * @param objectMapper the {@link ObjectMapper} to use for writing the error response
     */
    public ViravaAuthErrorEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Creates a new instance of {@link ViravaAuthErrorEntryPoint} with the given {@link ViravaFilterExceptionHandler}.
     * Use this constructor if you want to handle the error response yourself.
     *
     * @param viravaFilterExceptionHandler the {@link ViravaFilterExceptionHandler} to use for handling the error response
     */
    public ViravaAuthErrorEntryPoint(ViravaFilterExceptionHandler viravaFilterExceptionHandler) {
        this.viravaFilterExceptionHandler = viravaFilterExceptionHandler;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        if (viravaFilterExceptionHandler != null) {
            viravaFilterExceptionHandler.handle(request, response, authException);
        } else {
            var unauthorizedHttpStatus = HttpStatus.UNAUTHORIZED;
            response.setStatus(unauthorizedHttpStatus.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            var errorResponse = new ViravaErrorResponse(
                    LocalDateTime.now(),
                    unauthorizedHttpStatus.value(),
                    unauthorizedHttpStatus.getReasonPhrase(),
                    authException.getMessage(),
                    request.getRequestURI());
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }
}
