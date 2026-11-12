package com.company.aneon.shared.security;

import com.company.aneon.shared.auth.AuthService;
import com.company.aneon.shared.auth.SessionAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import com.company.aneon.shared.web.TraceIdFilter;

/**
 * Xac thuc JWT theo hop dong F16 khi cau hinh issuer; khi chua co identity
 * provider, profile local dung LocalIdentityFilter (stub co nhan, D0008).
 * CSRF tat vi xac thuc bang bearer header (rule 05).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final boolean jwtEnabled;
    private final String jwtIssuer;
    private final boolean localIdentityEnabled;
    private final AuthService authService;

    public SecurityConfig(@Value("${aneon.security.jwt-enabled:false}") boolean jwtEnabled,
                          @Value("${aneon.security.jwt-issuer:}") String jwtIssuer,
                          @Value("${aneon.security.local-identity-enabled:false}") boolean localIdentityEnabled,
                          AuthService authService) {
        this.jwtEnabled = jwtEnabled;
        this.jwtIssuer = jwtIssuer;
        this.localIdentityEnabled = localIdentityEnabled;
        this.authService = authService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        // 401/403 cung format envelope, khong trong cho ControllerAdvice (rule 04)
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"code\":401,\"message\":\"Chua xac thuc\",\"errorCode\":\"UNAUTHENTICATED\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"code\":403,\"message\":\"Khong co quyen truy cap\",\"errorCode\":\"ACCESS_DENIED\"}");
                        }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/f10/promotions-events/search").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/register",
                                "/api/v1/auth/logout", "/api/v1/auth/recover").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll());
        if (jwtEnabled && jwtIssuer != null && !jwtIssuer.isBlank()) {
            JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
            JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();
            converter.setJwtGrantedAuthoritiesConverter(jwt -> {
                var authorities = scopes.convert(jwt);
                Object roles = jwt.getClaim(AuthenticationAccessScopePort.ROLES_CLAIM);
                if (roles instanceof java.util.Collection<?> collection) {
                    var roleAuths = RolePermissions.authoritiesForRoles(collection.stream()
                            .map(String::valueOf).collect(java.util.stream.Collectors.toSet()));
                    var merged = new java.util.ArrayList<org.springframework.security.core.GrantedAuthority>(authorities);
                    merged.addAll(roleAuths);
                    return merged;
                }
                return authorities;
            });
            http.oauth2ResourceServer(oauth -> oauth
                    .jwt(jwt -> jwt.decoder(JwtDecoders.fromIssuerLocation(jwtIssuer))
                            .jwtAuthenticationConverter(converter)));
        }
        http.addFilterBefore(new TraceIdFilter(), SecurityContextHolderFilter.class);
        http.addFilterAfter(new SessionAuthenticationFilter(authService), SecurityContextHolderFilter.class);
        http.addFilterAfter(new LocalIdentityFilter(localIdentityEnabled), SessionAuthenticationFilter.class);
        return http.build();
    }
}
