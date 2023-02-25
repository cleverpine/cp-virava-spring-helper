package com.cleverpine.viravaspringhelper.core;

import com.cleverpine.viravaspringhelper.dto.Permission;

import java.util.List;

public interface BaseRole {

    String getRoleName();

    List<Permission> getPermissionList();

}
