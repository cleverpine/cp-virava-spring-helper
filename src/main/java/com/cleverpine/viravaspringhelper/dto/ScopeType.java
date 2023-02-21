package com.cleverpine.viravaspringhelper.dto;

public enum ScopeType {
    CREATE, READ, UPDATE, DELETE;

    public String scope() {
        return this.name();
    }
}
