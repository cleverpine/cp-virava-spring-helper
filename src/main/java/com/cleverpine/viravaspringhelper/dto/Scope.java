package com.cleverpine.viravaspringhelper.dto;

import java.util.List;

public enum Scope {
    CREATE(ScopeType.CREATE),
    READ(ScopeType.READ),
    UPDATE(ScopeType.UPDATE),
    DELETE(ScopeType.DELETE),
    CRU(ScopeType.CREATE, ScopeType.READ, ScopeType.UPDATE),
    CRUD(ScopeType.CREATE, ScopeType.READ, ScopeType.UPDATE, ScopeType.DELETE);

    private final List<ScopeType> scopeTypes;

    Scope(ScopeType... scopeTypes) {
        this.scopeTypes = List.of(scopeTypes);
    }

    public List<ScopeType> getScopeTypes() {
        return scopeTypes;
    }
}
