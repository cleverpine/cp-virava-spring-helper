package com.cleverpine.viravaspringhelper.config;

public class AuthTokenConfig {

    private final String usernamePath;

    private final String emailPath;

    private final String rolesPath;

    private final String isCompanyUserPath;

    private final String secret;

    private final String issuer;

    public AuthTokenConfig(String usernamePath, String rolesPath) {
        this.usernamePath = usernamePath;
        this.rolesPath = rolesPath;
        this.isCompanyUserPath = null;
        this.emailPath = null;
        this.secret = null;
        this.issuer = null;
    }

    public AuthTokenConfig(String usernamePath, String rolesPath, String isCompanyUserPath) {
        this.usernamePath = usernamePath;
        this.rolesPath = rolesPath;
        this.isCompanyUserPath = isCompanyUserPath;
        this.emailPath = null;
        this.secret = null;
        this.issuer = null;
    }

    public AuthTokenConfig(String usernamePath, String rolesPath, String secret, String issuer) {
        this.usernamePath = usernamePath;
        this.rolesPath = rolesPath;
        this.secret = secret;
        this.issuer = issuer;
        this.emailPath = null;
        this.isCompanyUserPath = null;
    }

    public AuthTokenConfig(String usernamePath, String emailPath, String rolesPath, String secret, String issuer, String isCompanyUserPath) {
        this.usernamePath = usernamePath;
        this.emailPath = emailPath;
        this.rolesPath = rolesPath;
        this.isCompanyUserPath = isCompanyUserPath;
        this.secret = secret;
        this.issuer = issuer;
    }

    public String getRolesPath() {
        return rolesPath;
    }

    public String getUsernamePath() {
        return usernamePath;
    }

    public String getEmailPath() {
        return emailPath;
    }

    public String getIsCompanyUserPath() {
        return isCompanyUserPath;
    }

    public String getSecret() {
        return secret;
    }

    public String getIssuer() {
        return issuer;
    }
}
