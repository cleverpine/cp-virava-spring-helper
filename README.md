# Virava (Authentication and Authorisation Library by CleverPine)

## Introduction

Virava is a simple and easy to use authentication and authorisation library for Java Spring Boot Applications.
It is a wrapper around Spring Security and provides a simple way to configure and use Spring Security in your
application with OAuth2.0 and OpenID Connect. It leverages the power of Sprint AOP to provide a simple and type-safe
way to define authorisation rules in your controllers.

## Features

Virava provides the following features:

- **Type-safe Authorisation Rules**: Define your resources and roles and then simply annotate your controller methods
  with the @ViravaSecured annotation.
```java
    @ViravaSecured(resource = Resources.PROJECT, scope = ScopeType.READ)
    public ResponseEntity<Project> getProject(Long projectId) {
        return ResponseEntity.ok(projectService.getProject(projectId));
    }
    
    @ViravaSecured(resource = Resources.PROJECT, scope = ScopeType.CREATE)
    public ResponseEntity<Project> createProject(Project project) {
        return ResponseEntity.ok(projectService.createProject(project));
    }
```

- **Error Handling**: Virava has integrated error handling for authentication and authorisation errors.
  All cases are handled with proper http status codes and error messages. Without a defined and registered
  ViravaAuthErrorEntryPoint bean, the default http status for both authentication and authorisation errors is 403 Forbidden. With
  it - 401 Unauthorized. Below configuration includes the bean.
- **Customisable User Principle Provider**: You can easily extend the ViravaPrincipleProvider to be able to access
  user specific data throughout the application via the SecurityContext.
- **Customisable Authentication Providers**: Virava is developed and tested with Keycloak, however, it can be easily
  configured to work with other OAuth2.0 and OpenID Connect providers.

### Integration

1. Add the dependency to your project
    ```xml
    <dependency>
        <groupId>com.cleverpine</groupId>
        <artifactId>cp-virava-spring-helper</artifactId>
        <version>3.1.2</version>
    </dependency>
    ```
2. Add to your application.properties or application.yml the following properties
    1. jwkSetUrl of your OAuth2.0 provider
    ```yaml
    auth:
      token:
        jwkSetUrl: https://sso.cleverpine.com/auth/realms/Virava/protocol/openid-connect/certs
    ```
    2. The path in the JWT payload of the roles
    ```yaml
    auth:
      token:
        rolesPath: resource_access.cleverpine.roles
    ```
    3. The path in the JWT payload of the username
    ```yaml
    auth:
      token:
        usernamePath: preferred_username
    ```
3. Create an enum for your resources. It has to implement the BaseResource interface.
   Create one enum value for each resource.
    ```java
    @RequiredArgsConstructor
    public enum Resources implements BaseResource {
        PROJECT("PROJECT"),
        SIMULATION("SIMULATION"),
        CUSTOMER("CUSTOMER"),
        WAREHOUSE("WAREHOUSE");
    
        private final String resource;
        @Override
        public String resource() {
            return toString();
        }
    
        @Override
        public List<BaseResource> getFullResourceList() {
            return Arrays.stream(Resources.values()).collect(Collectors.toList());
        }
    }
    ```
4. Create an enum for your roles. It has to implement the BaseRole interface.
   Create one enum value for each role that you have defined in your OAuth2.0 provider configuration.
    ```java
    @RequiredArgsConstructor
    public enum Roles implements BaseRole {
        USER("read", List.of(
            Permission.of(Resources.PROJECT, Scope.READ),
            Permission.of(Resources.SIMULATION, Scope.READ),
            Permission.of(Resources.CUSTOMER, Scope.READ),
            Permission.of(Resources.WAREHOUSE, Scope.READ)
        )),
        ADMIN("admin", List.of(
            Permission.of(Resources.PROJECT, Scope.CRUD),
            Permission.of(Resources.SIMULATION, Scope.CRUD),
            Permission.of(Resources.CUSTOMER, Scope.CRUD),
            Permission.of(Resources.WAREHOUSE, Scope.CRUD)
        ));

        private final String roleName;
        private final List<Permission> permissionList;

        @Override
        public String getRoleName() {
            return roleName;
        }
    
        @Override
        public List<Permission> getPermissionList() {
            return List.copyOf(permissionList);
        }
    }
    ```
5. Create the @ViravaSecuredAnnotation. Just copy the following code to your project.
    ```java
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ViravaSecured {
        Resources resource();
        ScopeType[] scope();
    }

    ```
6. Create the ViravaSecuredAspect. Just copy the following code to your project.
    ```java
    @Aspect
    @Component
    public class ViravaSecuredAspect extends BaseViravaSecuredAspect {

        public ViravaSecuredAspect(PrincipalProvider principalProvider) {
            super(principalProvider);
        }

        @Before("@annotation(viravaSecured)")
        public void secure(JoinPoint joinPoint, ViravaSecured viravaSecured) {
            authorize(joinPoint, viravaSecured.resource(), viravaSecured.scope());
        }
    }
    ```
7. Create your DTO to keep your user information. It has to implement the CustomPrincipalInfo interface.
8. Create your custom PrincipalProvider. It has to implement the ViravaPrincipalProvider interface.
   You need to implement the `provideCustomPrincipalInfo(String var1)` method.
   It will be called only once when the `getCustomPrincipalInfo()` method is called for the first time, where the `var1`
   parameter is the value of the `usernamePath` property in your application.properties or application.yml file in step
   2.3.
   You can inject other services in your custom PrincipalProvider if you need to enhance the user information with
   information from you database, for example. You can access the payload of the JWT token with
   the `getAuthentication()`
   method.
9. Create a ViravaHelperConfig class. Just copy the following code to your project.
    ```java
    @Setter
    @Configuration
    @ConfigurationProperties(prefix = "auth.token")
    public class ViravaHelperConfig {

        private String usernamePath;
        private String rolesPath;
        private String jwkSetUrl;

        @Bean
        public RoleConfig<Roles, Resources> roleConfig() {
           return new RoleConfig<>(Roles.values(), Resources.values());
        }

        @Bean
        public TokenAuthenticator<DecodedJWT> tokenAuthenticator() {
         return new ViravaJwtVerifier(authTokenConfig());
        }

        @Bean
        public AuthTokenConfig authTokenConfig() {
            return AuthTokenConfig.builder()
                   .withUsernamePath(usernamePath)
                   .withRolesPath(rolesPath)
                   .withJwkSetUrl(jwkSetUrl)
                   .build();
        }
        
        @Bean
        public ViravaFilter viravaFilter(RoleConfig<Roles, Resources> roleConfig, ObjectMapper objectMapper, AuthTokenConfig authTokenConfig) {
           return new ViravaFilter(roleConfig, objectMapper, authTokenConfig, tokenAuthenticator());
        }

        @Bean
        public ExceptionTypeMapper exceptionTypeMapper() {
            return new BaseExceptionTypeMapper();
        }
   
       @Bean
       public ViravaAuthErrorEntryPoint viravaAuthErrorEntryPoint(ObjectMapper objectMapper) {
            return new ViravaAuthErrorEntryPoint(objectMapper);
        }
    }
    ```
10. Register your ViravaFilter & ViravaAuthErrorEntryPoint in you security configuration.
   ```java
   @Configuration
   @EnableWebSecurity
   @AllArgsConstructor
   public class SecurityConfig {
   
       private final ViravaFilter authoritiesFilter;
       private final ViravaAuthErrorEntryPoint viravaAuthErrorEntryPoint;
   
       @Bean
       public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
           http
                   .authorizeHttpRequests(authz -> authz
                           .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/*/*.yml", "/api/system/*")
                           .permitAll()
                           .anyRequest()
                           .authenticated())
                   .exceptionHandling(eh -> eh.authenticationEntryPoint(viravaAuthErrorEntryPoint))
                   .csrf(CsrfConfigurer::disable)
                   .addFilterAfter(authoritiesFilter, BasicAuthenticationFilter.class)
                   .sessionManagement(session -> session
                           .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                   );
   
           return http.build();
       }
   }
   ```
11. Finally, annotate your controller methods with the @ViravaSecured annotation.
