package com.cleverpine.viravaspringhelper.aop;

import com.cleverpine.cpspringerrorutil.exception.AuthorisationException;
import com.cleverpine.viravaspringhelper.core.BaseResource;
import com.cleverpine.viravaspringhelper.core.ViravaAuthenticationToken;
import com.cleverpine.viravaspringhelper.core.ViravaPrincipalProvider;
import com.cleverpine.viravaspringhelper.core.ViravaUserPrincipal;
import com.cleverpine.viravaspringhelper.dto.ScopeType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

public class BaseViravaSecuredAspect {

    private final ViravaPrincipalProvider<?> viravaPrincipalProvider;

    public BaseViravaSecuredAspect(ViravaPrincipalProvider<?> viravaPrincipalProvider) {
        this.viravaPrincipalProvider = viravaPrincipalProvider;
    }

    protected void authorize(
            JoinPoint joinPoint,
            BaseResource resource,
            String resourceIdParamName,
            boolean requireAllResourceIds,
            ScopeType[] scopeList) {
        var authentication = viravaPrincipalProvider.getAuthentication()
                .orElseThrow(() -> new AuthorisationException("Invalid SecurityContextHolder"));
        var principal = authentication.getPrincipal();
        if (principal == null) {
            throw new AuthorisationException("Missing ViravaUserPrincipal on method requiring authorisation");
        }

        Long resourceId = getMethodSuppliedResourceId(joinPoint, resourceIdParamName);

        if (!principal.isAuthorized(resource, resourceId, requireAllResourceIds, scopeList)) {
            throw new AuthorisationException("User doesn't have required permissions");
        }
    }

    protected void authorize(JoinPoint joinPoint, BaseResource resource, ScopeType[] scopeList) {
        ViravaAuthenticationToken authentication = this.viravaPrincipalProvider.getAuthentication()
                .orElseThrow(() -> new AuthorisationException("Invalid SecurityContextHolder"));
        ViravaUserPrincipal principal = authentication.getPrincipal();
        if (principal == null) {
            throw new AuthorisationException("Missing ViravaUserPrincipal");
        } else if (!principal.isAuthorized(resource, scopeList)) {
            throw new AuthorisationException("User doesn't have required permissions");
        }
    }

    private Long getMethodSuppliedResourceId(JoinPoint joinPoint, String resourceIdParamName) {
        if (resourceIdParamName == null || resourceIdParamName.isEmpty()) {
            return null;
        }

        var methodSig = (MethodSignature) joinPoint.getSignature();
        var parameters = methodSig.getParameterNames();
        if (parameters == null) {
            throw new AssertionError("ViravaSecuredAsspect::authorize called on a method without parameters");
        }

        var args = joinPoint.getArgs();
        if (args == null) {
            throw new AssertionError("ViravaSecuredAsspect::authorize called without arguments");
        }

        if (args.length != parameters.length) {
            throw new AssertionError("ViravaSecuredAsspect::authorize parameter count does not match arg count");
        }

        Long resourceId = null;
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].equals(resourceIdParamName)) {
                var arg = args[i];
                if (arg instanceof Long) {
                    resourceId = (Long) arg;
                }
                break;
            }
        }

        return resourceId;
    }

}
