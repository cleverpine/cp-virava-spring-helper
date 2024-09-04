package com.cleverpine.viravaspringhelper.error;

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
    private Supplier<String> responseBodySupplier;

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
     * Creates a new instance of {@link ViravaAuthErrorEntryPoint} with the given {@link Supplier}.
     * Use this constructor if you want to provide a custom error response.
     *
     * @param responseBodySupplier the supplier to provide the error response
     */
    public ViravaAuthErrorEntryPoint(Supplier<String> responseBodySupplier) {
        this.responseBodySupplier = responseBodySupplier;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        var unauthorizedHttpStatus = HttpStatus.UNAUTHORIZED;
        response.setStatus(unauthorizedHttpStatus.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(getResponseBody(unauthorizedHttpStatus, authException.getMessage(), request.getRequestURI()));
    }

    private String getResponseBody(HttpStatus status, String message, String requestUri) throws JsonProcessingException {
        String responseBody;
        if (responseBodySupplier != null) {
            responseBody = responseBodySupplier.get();
        } else {
            var errorResponse = new ViravaErrorResponse(
                    LocalDateTime.now(),
                    status.value(),
                    status.getReasonPhrase(),
                    message,
                    requestUri);
            responseBody = objectMapper.writeValueAsString(errorResponse);
        }
        return responseBody;
    }
}
