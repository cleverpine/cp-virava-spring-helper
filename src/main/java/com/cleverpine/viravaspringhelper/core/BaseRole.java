package com.cleverpine.viravaspringhelper.core;

import com.cleverpine.viravaspringhelper.dto.Permission;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface BaseRole {

    String getRoleName();

    List<Permission> getPermissionList();

    static List<Permission> of(Permission... permissions) {
        return Arrays.stream(permissions).collect(Collectors.toUnmodifiableList());
    }

}
