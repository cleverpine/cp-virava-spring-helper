package com.cleverpine.viravaspringhelper.provider;

import com.cleverpine.viravaspringhelper.core.ViravaAuthenticationToken;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class ViravaTokenAuthenticationProvider implements AuthenticationProvider, InitializingBean {

    // Token verification
    // Principal provider

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // verify the token
        // principal provider . get authentication principal
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ViravaAuthenticationToken.class.isAssignableFrom(authentication);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        // assert if the properties / fields are set
    }
}
