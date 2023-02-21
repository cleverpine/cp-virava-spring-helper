package com.cleverpine.viravaspringhelper.dto;

import com.cleverpine.viravaspringhelper.core.BaseResource;
import java.util.Collections;
import java.util.List;

public class ResourceIdsAccess {

    private final BaseResource resource;

    private final List<Long> idsAccess;

    private final boolean canAccessAll;

    public ResourceIdsAccess(BaseResource resource, List<Long> idsAccess, boolean canAccessAll) {
        this.resource = resource;
        this.idsAccess = idsAccess;
        this.canAccessAll = canAccessAll;
    }

    public BaseResource getResource() {
        return resource;
    }

    public List<Long> getIdsAccess() {
        return Collections.unmodifiableList(idsAccess);
    }

    public boolean canAccessAll() {
        return canAccessAll;
    }
}
