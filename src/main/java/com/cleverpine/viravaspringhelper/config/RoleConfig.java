package com.cleverpine.viravaspringhelper.config;

import com.cleverpine.viravaspringhelper.core.BaseResource;
import com.cleverpine.viravaspringhelper.core.BaseRole;
import com.cleverpine.viravaspringhelper.dto.Permission;
import com.cleverpine.viravaspringhelper.dto.PermissionInitializer;
import com.cleverpine.viravaspringhelper.dto.ScopeType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RoleConfig<R extends BaseRole, RE extends BaseResource> {

    private final Map<String, Map<BaseResource, Set<ScopeType>>> permissionMap;

    private final List<RE> resourceList;

    public RoleConfig(R[] roleArray, RE[] resourceArray) {
        if (roleArray == null || roleArray.length == 0) {
            throw new AssertionError("Role array cannot be null or empty!");
        }
        if (resourceArray == null || resourceArray.length == 0) {
            throw new AssertionError("Resource array cannot be null or empty!");
        }
        this.permissionMap = getPermissionMap(List.of(roleArray));
        this.resourceList = List.of(resourceArray);
    }

    public RoleConfig(List<R> roleList, List<RE> resourceList) {
        if (roleList == null || roleList.isEmpty()) {
            throw new AssertionError("Role list cannot be null or empty!");
        }
        if (resourceList == null || resourceList.isEmpty()) {
            throw new AssertionError("Resource list cannot be null or empty!");
        }
        this.permissionMap = getPermissionMap(roleList);
        this.resourceList = Collections.unmodifiableList(resourceList);
    }

    public RoleConfig(R[] roleArray, RE[] resourceArray, Map<String, List<String>> roleMapping) {
        if (roleArray == null || roleArray.length == 0) {
            throw new AssertionError("Role array cannot be null or empty!");
        }
        if (resourceArray == null || resourceArray.length == 0) {
            throw new AssertionError("Resource array cannot be null or empty!");
        }
        if (roleMapping == null) {
            throw new AssertionError("Role mapping cannot be null or empty!");
        }
        this.permissionMap = getPermissionMap(List.of(roleArray), roleMapping);
        this.resourceList = List.of(resourceArray);
    }

    private Map<String, Map<BaseResource, Set<ScopeType>>> getPermissionMap(List<R> roleList) {
        return roleList.stream().collect(Collectors.toUnmodifiableMap(
                R::getRoleName, r -> getResourceScopeMap(r.getPermissionList())));
    }

    private Map<String, Map<BaseResource, Set<ScopeType>>> getPermissionMap(List<R> roleList, Map<String, List<String>> roleMappings) {
        var permissionMap = new HashMap<String, Map<BaseResource, Set<ScopeType>>>();

        for (R role : roleList) {
            for (String customRole : roleMappings.get(role.getRoleName().toLowerCase())) {
                permissionMap.put(customRole.toUpperCase(), getResourceScopeMap(role.getPermissionList()));
            }
        }

        return permissionMap;
    }

    private Map<BaseResource, Set<ScopeType>> getResourceScopeMap(List<Permission> permissionList) {
        return permissionList.stream().collect(Collectors.toMap(Permission::getResource, Permission::getScopeTypes));
    }

    private Map<BaseResource, PermissionInitializer> getDefaultPermissionList() {
        return resourceList.stream()
                .collect(Collectors.toMap(Function.identity(), PermissionInitializer::new));
    }

    public List<Permission> getRolePermissionList(List<String> roleList) {
        if (roleList == null) {
            return null;
        }
        var builderMap = getDefaultPermissionList();
        for(String role : roleList) {
            var resourceScopes = permissionMap.get(role);
            if (resourceScopes == null) {
                continue;
            }
            for (Map.Entry<BaseResource, Set<ScopeType>> entry : resourceScopes.entrySet()) {
                var builder = builderMap.get(entry.getKey());
                if (builder == null) {
                    throw new AssertionError(String.format(
                            "Improperly configured RoleConfig! "
                            + "It has to be supplied with the full list of Resources."
                            + "Resource [%s] is missing.", entry.getKey().resource()));
                }
                builder.addScope(entry.getValue());
            }
        }
        return builderMap.values().stream().map(Permission::of).collect(Collectors.toList());
    }

}
