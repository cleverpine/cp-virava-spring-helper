package com.cleverpine.viravaspringhelper.core;

import com.cleverpine.viravaspringhelper.config.AuthTokenConfig;
import com.cleverpine.viravaspringhelper.config.RoleConfig;
import com.cleverpine.viravaspringhelper.dto.Permission;
import com.cleverpine.viravaspringhelper.dto.ResourceIdsAccess;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.util.Assert;

public class ViravaAuthenticationToken extends AbstractAuthenticationToken {

    private static final String ACCESS_ALL_RESOURCE_IDS = "ALL";

    private static final Pattern numberPattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    private final ViravaUserPrincipal userPrincipal;

    private final Map<String, Object> payloadJsonMap;

    private final String tokenString;

    private final List<String> roles;

    private ViravaAuthenticationToken(ViravaUserPrincipal userPrincipal, String tokenString, List<String> roles, Map<String, Object> payloadJsonMap) {
        super(userPrincipal == null ? null : userPrincipal.getAuthorityList());
        this.userPrincipal = userPrincipal;
        this.tokenString = tokenString;
        this.roles = roles;
        this.payloadJsonMap = payloadJsonMap;
        super.setAuthenticated(true);
    }

    public static ViravaAuthenticationToken ofUnauthorised(String tokenString) {
        return new ViravaAuthenticationToken(null, tokenString, Collections.emptyList(), new HashMap<>());
    }

    public static ViravaAuthenticationToken ofAuthorised(
            Map<String, Object> payloadJsonMap,
            RoleConfig<?, ?> roleConfig,
            AuthTokenConfig authTokenConfig,
            String originalToken) {
        var username = extractStringFromJson(authTokenConfig.getUsernamePath(), payloadJsonMap);
        var email = extractStringFromJson(authTokenConfig.getEmailPath(), payloadJsonMap);
        var roles = extractListFromJson(authTokenConfig.getRolesPath(), payloadJsonMap);
        var isCompanyUser = extractBooleanFromJson(authTokenConfig.getIsCompanyUserPath(), payloadJsonMap);
        var permissionList = roleConfig.getRolePermissionList(roles);
        LinkedList<ResourceIdsAccess> resourceIdsAccessList = getResourceIdsAccesses(payloadJsonMap, permissionList);
        return new ViravaAuthenticationToken(
                new ViravaUserPrincipal(username, email, permissionList, resourceIdsAccessList, isCompanyUser), originalToken, roles, payloadJsonMap);
    }

    private static LinkedList<ResourceIdsAccess> getResourceIdsAccesses(Map<String, Object> payloadJsonMap, List<Permission> permissionList) {
        var resourceIdsAccessList = new LinkedList<ResourceIdsAccess>();
        if (permissionList != null) {
            for (Permission permission : permissionList) {
                var idsListParamString = BaseResource.getIdsParameterName(permission.getResource());
                if (payloadJsonMap.containsKey(idsListParamString)) {
                    String idsListString = extractStringFromJson(idsListParamString, payloadJsonMap);
                    if (idsListString != null) {
                        var idsList = idsListString.split(", ");
                        var canAccessAll = idsListString.contains(ACCESS_ALL_RESOURCE_IDS);
                        List<Long> accessIds = new LinkedList<>();
                        if (!canAccessAll && idsList != null) {
                            for (String idString : idsList) {
                                if (numberPattern.matcher(idString).matches()) {
                                    accessIds.add(Long.valueOf(idString));
                                }
                            }
                        }
                        resourceIdsAccessList.add(new ResourceIdsAccess(permission.getResource(), accessIds, canAccessAll));
                    }
                }
            }
        }
        return resourceIdsAccessList;
    }

    @Override
    public String getCredentials() {
        return this.tokenString;
    }

    @Override
    public ViravaUserPrincipal getPrincipal() {
        return this.userPrincipal;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        Assert.isTrue(!isAuthenticated, "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        super.setAuthenticated(false);
    }

    public List<String> getRoles() {
        return List.copyOf(roles);
    }

    public String getJsonAttr(String path) {
        return extractStringFromJson(path, this.payloadJsonMap);
    }

    public List<String> getJsonListAttr(String path) {
        return extractListFromJson(path, this.payloadJsonMap);
    }

    private static String extractStringFromJson(String path, Map<String, Object> jsonMap) {
        if (path == null) {
            return null;
        }
        var pathList = path.split("\\.");
        var param = readJsonValue(pathList, 0, jsonMap);
        if (param instanceof String) {
            return (String) param;
        }
        return null;
    }

    private static Boolean extractBooleanFromJson(String path, Map<String, Object> jsonMap) {
        if (path == null) {
            return null;
        }
        var pathList = path.split("\\.");
        var param = readJsonValue(pathList, 0, jsonMap);
        if (param instanceof Boolean) {
            return (Boolean) param;
        }
        return null;
    }

    private static List<String> extractListFromJson(String path, Map<String, Object> jsonMap) {
        var pathList = path.split("\\.");
        var param = readJsonValue(pathList, 0, jsonMap);
        if (param instanceof List && ((List<?>) param).get(0) instanceof String) {
            return ((List<?>) param).stream().map(s -> (String) s).collect(Collectors.toList());
        }
        return null;
    }

    private static Object readJsonValue(String[] pathList, int index, Map jsonMap) {
        var result = jsonMap.get(pathList[index]);
        if ((index + 1) == pathList.length) {
            return result;
        } else if (result instanceof Map) {
            return readJsonValue(pathList, ++index, (Map) result);
        } else {
            return null;
        }
    }
}
