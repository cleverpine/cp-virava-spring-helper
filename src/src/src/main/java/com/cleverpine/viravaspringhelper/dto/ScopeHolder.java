package com.cleverpine.viravaspringhelper.dto;

public class ScopeHolder {
    private final boolean create;
    private final boolean read;
    private final boolean update;
    private final boolean delete;

    public ScopeHolder(boolean create, boolean read, boolean update, boolean delete) {
        this.create = create;
        this.read = read;
        this.update = update;
        this.delete = delete;
    }

    public boolean canCreate() {
        return create;
    }

    public boolean canRead() {
        return read;
    }

    public boolean canUpdate() {
        return update;
    }

    public boolean canDelete() {
        return delete;
    }

}