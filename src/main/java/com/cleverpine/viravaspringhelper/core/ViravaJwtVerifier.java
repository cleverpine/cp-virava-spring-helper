package com.cleverpine.viravaspringhelper.core;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.cleverpine.viravaspringhelper.config.AuthTokenConfig;
import com.cleverpine.viravaspringhelper.error.exception.ViravaAuthenticationException;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ViravaJwtVerifier implements TokenAuthenticator<DecodedJWT> {

    private static final Integer JWK_SETS_CACHE_SIZE = 10;
    private static final Integer JWK_SET_CACHE_DURATION_HOURS = 24;
    private static final String JWT_VERIFIER_MISSING_REQUIRED_DATA =
            "Cannot initialize a new instance because one of jwk-set-url or secret should be present";

    private final AuthTokenConfig authTokenConfig;
    private final JwkProvider jwkProvider;

    public ViravaJwtVerifier(AuthTokenConfig authTokenConfig) {
        requireOnlyOneVerificationMethod(authTokenConfig);
        this.authTokenConfig = authTokenConfig;
        jwkProvider = initializeJwkProvider(authTokenConfig.getJwkSetUrl());
    }

    @Override
    public DecodedJWT process(String token) {
        try {
            return isJwkSetProviderInitialized() ?
                    verifyTokenAsymmetrically(token) :
                    verifyTokenSymmetrically(token);
        } catch (JWTVerificationException | JwkException exception) {
            throw new ViravaAuthenticationException(exception.getMessage(), exception);
        }
    }

    private DecodedJWT verifyTokenAsymmetrically(String token) throws JwkException {
        var jwt = JWT.decode(token);
        var jwk = jwkProvider.get(jwt.getKeyId());
        var publicKey = (RSAPublicKey) jwk.getPublicKey();
        var verificationAlgorithm = Algorithm.RSA256(publicKey, null);
        return verify(token, verificationAlgorithm);
    }

    private DecodedJWT verifyTokenSymmetrically(String token) {
        return verify(token, Algorithm.HMAC256(authTokenConfig.getSecret()));
    }

    private DecodedJWT verify(String token, Algorithm algorithm) {
        var jwtVerification = JWT.require(algorithm);
        if (Objects.nonNull(authTokenConfig.getIssuer())) {
            jwtVerification.withIssuer(authTokenConfig.getIssuer());
        }
        var jwtVerifier = jwtVerification
                .build();
        return jwtVerifier.verify(token);
    }

    private JwkProvider initializeJwkProvider(String jwkSetUrl) {
        if (Objects.isNull(jwkSetUrl)) {
            return null;
        }
        try {
            return new JwkProviderBuilder(new URL(jwkSetUrl))
                    .cached(JWK_SETS_CACHE_SIZE, JWK_SET_CACHE_DURATION_HOURS, TimeUnit.HOURS)
                    .build();
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    private boolean isJwkSetProviderInitialized() {
        return Objects.nonNull(jwkProvider);
    }

    private void requireOnlyOneVerificationMethod(AuthTokenConfig authTokenConfig) {
        var jwkSetUrl = authTokenConfig.getJwkSetUrl();
        var secret = authTokenConfig.getSecret();
        if (Objects.nonNull(jwkSetUrl) && Objects.nonNull(secret)) {
            throw new IllegalArgumentException(JWT_VERIFIER_MISSING_REQUIRED_DATA);
        }
        if (Objects.isNull(jwkSetUrl) && Objects.isNull(secret)) {
            throw new IllegalArgumentException(JWT_VERIFIER_MISSING_REQUIRED_DATA);
        }
    }
}
