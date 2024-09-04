package com.cleverpine.viravaspringhelper.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.cleverpine.viravaspringhelper.config.AuthTokenConfig;
import com.cleverpine.viravaspringhelper.config.RoleConfig;
import com.cleverpine.viravaspringhelper.core.ViravaFilterExceptionHandler;
import com.cleverpine.viravaspringhelper.core.TokenAuthenticator;
import com.cleverpine.viravaspringhelper.core.ViravaAuthenticationToken;
import com.cleverpine.viravaspringhelper.error.exception.ViravaAuthenticationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

public class ViravaFilter extends OncePerRequestFilter {

    public static final String BEARER_PREFIX = "Bearer ";

    private final RoleConfig<?, ?> roleConfig;
    private final ObjectMapper objectMapper;
    private final AuthTokenConfig authTokenConfig;
    private final TokenAuthenticator<DecodedJWT> tokenAuthenticator;
    private ViravaFilterExceptionHandler filterExceptionHandler;

    public ViravaFilter(RoleConfig<?, ?> roleConfig, ObjectMapper objectMapper, AuthTokenConfig authTokenConfig, TokenAuthenticator<DecodedJWT> tokenAuthenticator) {
        this.roleConfig = roleConfig;
        this.objectMapper = objectMapper;
        this.authTokenConfig = authTokenConfig;
        this.tokenAuthenticator = tokenAuthenticator;
    }

    /**
     * Sets a consumer to handle unauthorized responses.
     * If not set, the filter will return a 401 status code with the same content type as the request.
     * <p>
     * Example:
     * </p>
     * <pre>{@code
     * (request, response, ex) -> {
     *     var errorDetails = new ErrorDetails();
     *     errorDetails.setTimestamp(OffsetDateTime.now());
     *     errorDetails.setMessage(ex.getMessage());
     *     errorDetails.setPath(request.getRequestURI());
     *     response.setStatus(HttpStatus.UNAUTHORIZED.value());
     *     response.setContentType(MediaType.APPLICATION_JSON_VALUE);
     *     response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
     * }
     * }</pre>
     *
     * @param filterExceptionHandler the consumer to handle unauthorized responses
     */
    public void setFilterExceptionHandler(ViravaFilterExceptionHandler filterExceptionHandler) {
        this.filterExceptionHandler = filterExceptionHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
        var tokenString = authorizationHeader.substring(BEARER_PREFIX.length());
        try {
            var jwt = tokenAuthenticator.process(tokenString);
            var payload = new String(Base64.getUrlDecoder().decode(jwt.getPayload()));
            Map<String, Object> payloadJsonMap = objectMapper.readValue(payload, new TypeReference<>() {
            });
            var authentication = ViravaAuthenticationToken
                    .ofAuthorized(payloadJsonMap, roleConfig, authTokenConfig, tokenString);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (ViravaAuthenticationException | JsonProcessingException exception) {
            if (filterExceptionHandler == null) {
                var contentType = request.getHeader(HttpHeaders.ACCEPT);
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(contentType);
            } else {
                filterExceptionHandler.handle(request, response, exception);
            }
        }
    }
}
