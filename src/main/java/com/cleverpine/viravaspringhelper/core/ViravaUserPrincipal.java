package com.cleverpine.viravaspringhelper.core;

import com.cleverpine.viravaspringhelper.dto.Permission;
import com.cleverpine.viravaspringhelper.dto.ResourceIdsAccess;
import com.cleverpine.viravaspringhelper.dto.ScopeType;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ViravaUserPrincipal {

    private final String username;

    private String email;

    private Boolean isCompanyUser;

    private final Map<BaseResource, Set<ScopeType>> permissionMap;

    private final Map<BaseResource, ResourceIdsAccess> resourceResourceIdsAccessMap;

    private final List<Permission> permissionList;

    private final List<ViravaAuthority> authorityList;

    private CustomPrincipalInfo customPrincipalInfo;

    public ViravaUserPrincipal(String username, List<Permission> permissionList, List<ResourceIdsAccess> resourceIdsAccessList) {
        this.username = username;
        if (permissionList == null || permissionList.isEmpty()) {
            this.permissionList = Collections.unmodifiableList(new LinkedList<>());
            this.permissionMap = Collections.unmodifiableMap(new HashMap<>());
            this.authorityList = Collections.unmodifiableList(new LinkedList<>());
        } else {
            this.permissionList = permissionList;
            this.permissionMap = permissionList.stream()
                    .collect(Collectors.toUnmodifiableMap(Permission::getResource, Permission::getScopeTypes, (first, second) -> {
                        var mergedSet = new HashSet<ScopeType>();
                        mergedSet.addAll(first);
                        mergedSet.addAll(second);
                        return mergedSet;
                    }));
            this.authorityList = permissionList.stream().flatMap(p -> p.getScopeTypes().stream()
                    .map(s -> new ViravaAuthority(p.getResource(), s))).collect(Collectors.toUnmodifiableList());
        }
        if (resourceIdsAccessList == null || resourceIdsAccessList.isEmpty()) {
            this.resourceResourceIdsAccessMap = Collections.unmodifiableMap(new HashMap<>());
        } else {
            this.resourceResourceIdsAccessMap = resourceIdsAccessList.stream()
                    .collect(Collectors.toUnmodifiableMap(ResourceIdsAccess::getResource, Function.identity()));
        }
    }

    public ViravaUserPrincipal(String username, String email, List<Permission> permissionList, List<ResourceIdsAccess> resourceIdsAccessList) {
        this(username, permissionList, resourceIdsAccessList);
        this.email = email;
    }

    public ViravaUserPrincipal(String username, String email, List<Permission> permissionList, List<ResourceIdsAccess> resourceIdsAccessList, Boolean isCompanyUser) {
        this(username, permissionList, resourceIdsAccessList);
        this.email = email;
        this.isCompanyUser = isCompanyUser;
    }

    public <CPI extends CustomPrincipalInfo> void setCustomPrincipalInfo(CPI customPrincipalInfo) {
        this.customPrincipalInfo = customPrincipalInfo;
    }

    public <CPI extends CustomPrincipalInfo> CPI getCustomPrincipalInfo(Class<CPI> type) {
        if (Objects.isNull(customPrincipalInfo)) {
            return null;
        }
        return type.cast(customPrincipalInfo);
    }

    public List<Permission> getPermissionList() {
        return List.copyOf(permissionList);
    }

    public List<ViravaAuthority> getAuthorityList() {
        return List.copyOf(authorityList);
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getIsCompanyUser() {
        return isCompanyUser;
    }

    public boolean isAuthorized(BaseResource resource, ScopeType... requiredScopes) {
        if (resource == null || requiredScopes == null) {
            return true;
        }
        var resourceScopes = permissionMap.get(resource);
        if (resourceScopes == null || resourceScopes.isEmpty()) {
            return false;
        }
        return !Collections.disjoint(resourceScopes, Set.of(requiredScopes));
    }

    public boolean isAuthorized(BaseResource resource, Long resourceId, boolean requireAllResourceIds, ScopeType... requiredScopes) {
        if (resource == null || requiredScopes == null || requiredScopes.length == 0) {
            return true;
        }
        var resourceScopes = permissionMap.get(resource);
        if (resourceScopes == null || resourceScopes.isEmpty()) {
            return false;
        }
        if (Collections.disjoint(resourceScopes, Set.of(requiredScopes))) {
            return false;
        }
        var resourceIdsAccess = resourceResourceIdsAccessMap.get(resource);

        if (requireAllResourceIds) {
            return resourceIdsAccess != null && resourceIdsAccess.canAccessAll();
        }
        if (resourceId != null) {
            return resourceIdsAccess != null &&
                    (resourceIdsAccess.canAccessAll() || resourceIdsAccess.getIdsAccess().contains(resourceId));
        }
        return true;
    }

}
