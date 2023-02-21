package com.cleverpine.viravaspringhelper.core;

import com.cleverpine.viravaspringhelper.dto.ScopeType;
import org.springframework.security.core.GrantedAuthority;

public class ViravaAuthority implements GrantedAuthority {

    private final String authority;

    ViravaAuthority(BaseResource resource, ScopeType scope) {
        this.authority = String.format("%s_%s", resource.resource(), scope.scope()).toUpperCase();
    }

    @Override
    public String getAuthority() {
        return authority;
    }
}
