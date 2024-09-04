package com.cleverpine.viravaspringhelper.core;

import com.cleverpine.viravaspringhelper.error.exception.ViravaAuthenticationException;

public interface TokenAuthenticator<T> {

    /**
     * Authenticates the access token. It can decode, verify or check the token based on the provided implementation.
     * @param token the token to be authenticated or verified
     * @return the authenticated token
     * @throws ViravaAuthenticationException if there is a problem with the token authentication
     */
    T process(String token) throws ViravaAuthenticationException;
}
