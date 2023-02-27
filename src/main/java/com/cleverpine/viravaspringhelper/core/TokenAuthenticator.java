package com.cleverpine.viravaspringhelper.core;

public interface TokenAuthenticator<T> {

    /**
     * Authenticates the access token. It can decode, verify or check the token based on the provided implementation.
     * @param token the token to be authenticated or verified
     * @return the authenticated token
     * @throws com.cleverpine.viravaspringhelper.error.exception.ViravaAuthenticationException if there is a problem with the token authentication
     */
    T process(String token);
}
