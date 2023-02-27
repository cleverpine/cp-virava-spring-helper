package com.cleverpine.viravaspringhelper.config;

public class AuthTokenConfig {

    private final String usernamePath;

    private final String emailPath;

    private final String rolesPath;

    private final String isCompanyUserPath;

    private final String secret;

    private final String issuer;

    private final String jwkSetUrl;

    public AuthTokenConfig(String usernamePath,
                           String emailPath,
                           String rolesPath,
                           String secret,
                           String issuer,
                           String isCompanyUserPath,
                           String jwkSetUrl) {
        this.usernamePath = usernamePath;
        this.emailPath = emailPath;
        this.rolesPath = rolesPath;
        this.isCompanyUserPath = isCompanyUserPath;
        this.secret = secret;
        this.issuer = issuer;
        this.jwkSetUrl = jwkSetUrl;
    }

    public static Builder builder() {
        return new Builder();
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

    public String getJwkSetUrl() {
        return jwkSetUrl;
    }

    public static class Builder {
        private String usernamePath;

        private String emailPath;

        private String rolesPath;

        private String isCompanyUserPath;

        private String secret;

        private String issuer;

        private String jwkSetUrl;

        public Builder withUsernamePath(String usernamePath) {
            this.usernamePath = usernamePath;
            return this;
        }

        public Builder withEmailPath(String emailPath) {
            this.emailPath = emailPath;
            return this;
        }

        public Builder withRolesPath(String rolesPath) {
            this.rolesPath = rolesPath;
            return this;
        }

        public Builder withIsCompanyUserPath(String isCompanyUserPath) {
            this.isCompanyUserPath = isCompanyUserPath;
            return this;
        }

        public Builder withSecret(String secret) {
            this.secret = secret;
            return this;
        }

        public Builder withIssuer(String issuer) {
            this.issuer = issuer;
            return this;
        }

        public Builder withJwkSetUrl(String jwkSetUrl) {
            this.jwkSetUrl = jwkSetUrl;
            return this;
        }

        public AuthTokenConfig build() {
            return new AuthTokenConfig(
                    usernamePath,
                    emailPath,
                    rolesPath,
                    secret,
                    issuer,
                    isCompanyUserPath,
                    jwkSetUrl
            );
        }
    }
}
