package com.cleverpine.viravaspringhelper.core;

import java.util.List;

public interface BaseResource {

    static final String ID_ACCESS_LIST_PREFIX = "IDS_";

    static String getIdsParameterName(BaseResource resource) {
        return (ID_ACCESS_LIST_PREFIX + resource.resource()).toUpperCase();
    }

    String resource();

    List<BaseResource> getFullResourceList();
}
