package com.cleverpine.viravaspringhelper.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.cleverpine.viravaspringhelper.config.AuthTokenConfig;
import com.cleverpine.viravaspringhelper.config.RoleConfig;
import com.cleverpine.viravaspringhelper.core.ViravaAuthenticationToken;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class ViravaFilter extends OncePerRequestFilter {

    public static final String BEARER_PREFIX = "Bearer ";

    private final RoleConfig<?, ?> roleConfig;

    private final ObjectMapper objectMapper;

    private final AuthTokenConfig authTokenConfig;

    public ViravaFilter(RoleConfig<?, ?> roleConfig, ObjectMapper objectMapper, AuthTokenConfig authTokenConfig) {
        this.roleConfig = roleConfig;
        this.objectMapper = objectMapper;
        this.authTokenConfig = authTokenConfig;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            setUnauthorisedAuthContext(null);
            filterChain.doFilter(request, response);
            return;
        }
        var tokenString = authorizationHeader.substring(BEARER_PREFIX.length());
        try {
            DecodedJWT token = null;
            if (authTokenConfig.getSecret() == null) {
                token = JWT.decode(tokenString);
            } else {
                token = verify(tokenString, authTokenConfig.getSecret(), authTokenConfig.getIssuer());
            }
            var payload = new String(Base64.getDecoder().decode(token.getPayload()));
            Map<String, Object> payloadJsonMap = objectMapper.readValue(payload, new TypeReference<Map<String, Object>>() {
            });
            var authentication = ViravaAuthenticationToken
                    .ofAuthorised(payloadJsonMap, roleConfig, authTokenConfig, tokenString);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JWTVerificationException | JsonProcessingException exception) {
            setUnauthorisedAuthContext(tokenString);
            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void setUnauthorisedAuthContext(String token) {
        SecurityContextHolder.getContext().setAuthentication(ViravaAuthenticationToken.ofUnauthorised(token));
    }

    private DecodedJWT verify(String token, String secret, String issuer) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .build();
        return verifier.verify(token);
    }
}
