package com.company.aneon.shared.auth;

import java.time.Duration;
import java.util.UUID;

import com.company.aneon.shared.error.ApiException;
import com.company.aneon.shared.error.ErrorCode;
import com.company.aneon.shared.web.ApiResponse;
import com.company.aneon.shared.web.PageParams;
import com.company.aneon.shared.web.PageResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService service;
    private final AuthRateLimit limits;
    private final boolean secureCookies;

    AuthController(AuthService service, AuthRateLimit limits, @Value("${aneon.auth.secure-cookies:false}") boolean secureCookies) {
        this.service = service;
        this.limits = limits;
        this.secureCookies = secureCookies;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDtos.Account>> login(@Valid @RequestBody AuthDtos.Login credentials, HttpServletRequest request) {
        limits.attempt("login-client:" + request.getRemoteAddr(), 120);
        limits.attempt("login-user:" + AuthService.username(credentials.username()), 8);
        return signedIn(service.login(credentials, SessionAuthenticationFilter.cookie(request)), 200);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthDtos.Account>> register(@Valid @RequestBody AuthDtos.Register account, HttpServletRequest request) {
        limits.attempt("register-client:" + request.getRemoteAddr(), 30);
        return signedIn(service.register(account, SessionAuthenticationFilter.cookie(request)), 201);
    }

    @GetMapping("/me")
    public ApiResponse<AuthDtos.Account> me(Authentication authentication) {
        return ApiResponse.ok("Tai khoan hien tai", requireSession(authentication));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Boolean>> logout(HttpServletRequest request) {
        service.logout(SessionAuthenticationFilter.cookie(request));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .header(HttpHeaders.SET_COOKIE, cookie("", Duration.ZERO).toString())
                .body(ApiResponse.ok("Da dang xuat", true));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<AuthDtos.Account>> changePassword(Authentication authentication, @Valid @RequestBody AuthDtos.ChangePassword change) {
        UUID account = requireSession(authentication).id();
        limits.attempt("change:" + account, 10);
        return signedIn(service.changePassword(account, change), 200);
    }

    @PostMapping("/recovery-code")
    public ApiResponse<AuthDtos.RecoveryCode> recoveryCode(Authentication authentication, @Valid @RequestBody AuthDtos.ConfirmPassword confirmation) {
        UUID account = requireSession(authentication).id();
        limits.attempt("recovery-issue:" + account, 10);
        return ApiResponse.ok("Da tao ma khoi phuc", service.createRecovery(account, confirmation));
    }

    @PostMapping("/recover")
    public ApiResponse<Boolean> recover(@Valid @RequestBody AuthDtos.Recovery recovery, HttpServletRequest request) {
        limits.attempt("recover-client:" + request.getRemoteAddr(), 60);
        limits.attempt("recover-user:" + AuthService.username(recovery.username()), 8);
        return ApiResponse.ok("Da dat lai mat khau", service.resetWithRecovery(recovery));
    }

    @GetMapping("/staff")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ApiResponse<PageResponse<AuthDtos.StaffAccount>> staff(Authentication authentication,
            @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
        requireSession(authentication);
        return ApiResponse.ok("Danh sach nhan vien", service.staff(PageParams.page(page), PageParams.size(size)));
    }

    @PostMapping("/staff")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<AuthDtos.StaffCredentials>> createStaff(Authentication authentication, @Valid @RequestBody AuthDtos.StaffRequest request) {
        return ResponseEntity.status(201).cacheControl(CacheControl.noStore()).body(ApiResponse.created("Da tao tai khoan nhan vien",
                service.createStaff(requireSession(authentication).id(), request)));
    }

    @PutMapping("/staff/{id}/status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ApiResponse<AuthDtos.StaffAccount> staffStatus(Authentication authentication, @PathVariable UUID id,
            @Valid @RequestBody AuthDtos.SetAccountStatus request) {
        requireSession(authentication);
        return ApiResponse.ok("Da doi trang thai tai khoan", service.setStaffLocked(id, request.locked()));
    }

    @PostMapping("/staff/{id}/password-reset")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<AuthDtos.StaffCredentials>> resetStaff(Authentication authentication, @PathVariable UUID id) {
        requireSession(authentication);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ApiResponse.ok("Da dat mat khau tam", service.resetStaffPassword(id)));
    }

    private ResponseEntity<ApiResponse<AuthDtos.Account>> signedIn(AuthService.SignedIn result, int status) {
        return ResponseEntity.status(status).cacheControl(CacheControl.noStore())
                .header(HttpHeaders.SET_COOKIE, cookie(result.sessionToken(), AuthService.SESSION_LENGTH).toString())
                .body(new ApiResponse<>(status, "Da xac thuc tai khoan", result.account()));
    }

    private ResponseCookie cookie(String token, Duration duration) {
        return ResponseCookie.from(SessionAuthenticationFilter.COOKIE_NAME, token).httpOnly(true)
                .secure(secureCookies).sameSite("Lax").path("/").maxAge(duration).build();
    }

    private AuthDtos.Account requireSession(Authentication authentication) {
        if (authentication instanceof SessionAuthenticationFilter.SessionAuthentication session) return session.account();
        throw new ApiException(ErrorCode.UNAUTHENTICATED, "Hay dang nhap bang tai khoan ANEON");
    }
}
