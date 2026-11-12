package com.company.aneon.shared.auth;

import java.io.IOException;
import java.util.Set;

import com.company.aneon.shared.security.AneonPrincipal;
import com.company.aneon.shared.security.RolePermissions;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SessionAuthenticationFilter extends OncePerRequestFilter {
    static final String COOKIE_NAME = "aneon_session";
    private static final String AUTH = "/api/v1/auth/";
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");
    private static final Set<String> PASSWORD_CHANGE_PATHS = Set.of(AUTH + "me", AUTH + "password", AUTH + "logout", AUTH + "login",
            "/api/v1/f10/promotions-events/search");
    private final AuthService auth;

    public SessionAuthenticationFilter(AuthService auth) { this.auth = auth; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getServletPath();
        if (path.isEmpty()) path = request.getRequestURI().substring(request.getContextPath().length());
        String token = cookie(request);
        if (!SAFE_METHODS.contains(request.getMethod()) && (token != null || path.startsWith(AUTH))
                && (!"aneon-web".equals(request.getHeader("X-ANEON-Request")) || "cross-site".equals(request.getHeader("Sec-Fetch-Site")))) {
            deny(response, "CSRF_REJECTED", "Yeu cau khong xac minh duoc nguon gui");
            return;
        }
        if (token != null && request.getHeader("Authorization") == null) {
            var account = auth.session(token);
            if (account.isPresent()) {
                if (path.startsWith("/api/") && account.get().mustChangePassword() && !PASSWORD_CHANGE_PATHS.contains(path)) {
                    deny(response, "PASSWORD_CHANGE_REQUIRED", "Hay doi mat khau tam truoc khi su dung nghiep vu");
                    return;
                }
                SecurityContextHolder.getContext().setAuthentication(new SessionAuthentication(account.get()));
            }
        }
        chain.doFilter(request, response);
    }

    static String cookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) if (COOKIE_NAME.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }

    private static void deny(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(403);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"message\":\"" + message + "\",\"errorCode\":\"" + code + "\"}");
    }

    static final class SessionAuthentication extends AbstractAuthenticationToken {
        private final AuthDtos.Account account;
        private final AneonPrincipal principal;

        SessionAuthentication(AuthDtos.Account account) {
            super(RolePermissions.authoritiesForRoles(Set.of(account.role())));
            this.account = account;
            this.principal = new AneonPrincipal(account.id(), account.username(), Set.of(account.role()));
            super.setAuthenticated(true);
        }

        AuthDtos.Account account() { return account; }
        @Override public AneonPrincipal getPrincipal() { return principal; }
        @Override public Object getCredentials() { return null; }
    }
}
