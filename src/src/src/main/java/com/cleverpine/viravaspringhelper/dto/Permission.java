package com.cleverpine.viravaspringhelper.dto;

import com.cleverpine.viravaspringhelper.core.BaseResource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Permission {
    private final BaseResource resource;

    private final Set<ScopeType> scopeTypes;

    private final ScopeHolder scopeHolder;

    private Permission(BaseResource resource, Set<ScopeType> scopeTypes) {
        this.resource = resource;
        this.scopeTypes = Collections.unmodifiableSet(scopeTypes);
        this.scopeHolder = new ScopeHolder(
                scopeTypes.contains(ScopeType.CREATE),
                scopeTypes.contains(ScopeType.READ),
                scopeTypes.contains(ScopeType.UPDATE),
                scopeTypes.contains(ScopeType.DELETE));
    }

    public static Permission of(PermissionBuilder permissionBuilder) {
        return new Permission(permissionBuilder.getResource(), permissionBuilder.getScopeTypes());
    }

    public static Permission of(BaseResource resource, Scope... scopes) {
        var scopeTypes = scopes == null ?
                new HashSet<ScopeType>() :
                Arrays.stream(scopes).flatMap(scope -> scope.getScopeTypes().stream()).collect(Collectors.toSet());
        return new Permission(resource, scopeTypes);
    }

    public BaseResource getResource() {
        return resource;
    }

    public Set<ScopeType> getScopeTypes() {
        return Set.copyOf(scopeTypes);
    }

    public ScopeHolder getScopeHolder() {
        return scopeHolder;
    }
}
