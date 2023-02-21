package com.cleverpine.viravaspringhelper.core;

import java.util.Optional;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class ViravaPrincipalProvider<CPI extends CustomPrincipalInfo> {

    private final Class<CPI> cpiClass;

    protected ViravaPrincipalProvider(Class<CPI> cpiClass) {
        this.cpiClass = cpiClass;
    }

    public abstract CPI provideCustomPrincipalInfo(String username);

    public final Optional<ViravaAuthenticationToken> getAuthentication() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof ViravaAuthenticationToken) {
            return Optional.of((ViravaAuthenticationToken) authentication);
        }
        return Optional.empty();
    }

    public final CPI getCustomPrincipalInfo() {
        var auth = getAuthentication().orElse(null);
        if (auth == null) {
            return null;
        }
        var principal = auth.getPrincipal();
        if (principal == null || principal.getUsername() == null || principal.getUsername().isEmpty()) {
            return null;
        }
        CPI customInfo = principal.getCustomPrincipalInfo(cpiClass);
        if (customInfo == null) {
            customInfo = provideCustomPrincipalInfo(principal.getUsername());
            principal.setCustomPrincipalInfo(customInfo);
        }
        return customInfo;
    }

}
