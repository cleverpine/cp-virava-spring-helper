package com.cleverpine.viravaspringhelper.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.cleverpine.viravaspringhelper.config.AuthTokenConfig;
import com.cleverpine.viravaspringhelper.config.RoleConfig;
import com.cleverpine.viravaspringhelper.core.TokenAuthenticator;
import com.cleverpine.viravaspringhelper.core.ViravaAuthenticationToken;
import com.cleverpine.viravaspringhelper.error.exception.InvalidTokenAuthenticationException;
import com.cleverpine.viravaspringhelper.error.exception.ViravaAuthenticationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

public class ViravaFilter extends OncePerRequestFilter {

    public static final String BEARER_PREFIX = "Bearer ";
    private static final String INVALID_ACCESS_TOKEN = "Authentication failed: Invalid access token";

    private final RoleConfig<?, ?> roleConfig;

    private final ObjectMapper objectMapper;

    private final AuthTokenConfig authTokenConfig;

    private final TokenAuthenticator<DecodedJWT> tokenAuthenticator;

    public ViravaFilter(RoleConfig<?, ?> roleConfig, ObjectMapper objectMapper, AuthTokenConfig authTokenConfig, TokenAuthenticator<DecodedJWT> tokenAuthenticator) {
        this.roleConfig = roleConfig;
        this.objectMapper = objectMapper;
        this.authTokenConfig = authTokenConfig;
        this.tokenAuthenticator = tokenAuthenticator;
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
            Map<String, Object> payloadJsonMap = objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {
            });
            var authentication = ViravaAuthenticationToken
                    .ofAuthorized(payloadJsonMap, roleConfig, authTokenConfig, tokenString);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (ViravaAuthenticationException | JsonProcessingException exception) {
            throw new InvalidTokenAuthenticationException(INVALID_ACCESS_TOKEN, exception);
        }

        filterChain.doFilter(request, response);
    }
}
