package com.cleverpine.viravaspringhelper.dto;

import com.cleverpine.viravaspringhelper.core.BaseResource;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.springframework.lang.NonNull;

public class PermissionBuilder {

    private final BaseResource resource;

    private final Set<ScopeType> scopeTypes;

    public PermissionBuilder(@NonNull BaseResource resource) {
        this.resource = resource;
        this.scopeTypes = new HashSet<>();
    }

    public BaseResource getResource() {
        return this.resource;
    }

    public Set<ScopeType> getScopeTypes() {
        return this.scopeTypes;
    }

    public PermissionBuilder addScope(@NonNull Collection<ScopeType> scopeTypes) {
        this.scopeTypes.addAll(scopeTypes);
        return this;
    }

    public PermissionBuilder addScope(@NonNull ScopeType scopeType) {
        this.scopeTypes.add(scopeType);
        return this;
    }

}
