package com.company.aneon.shared.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.company.aneon.shared.error.ApiException;
import com.company.aneon.shared.error.ErrorCode;
import com.company.aneon.shared.web.PageResponse;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    static final Duration SESSION_LENGTH = Duration.ofHours(8);
    private static final Set<String> STAFF_ROLES = Set.of("MANAGEMENT", "DIRECTOR", "OPERATIONS", "ACCOUNTING", "MARKETING");

    record SignedIn(AuthDtos.Account account, String sessionToken) { }
    private final SecureRandom random = new SecureRandom();
    private final Pbkdf2PasswordEncoder passwords = Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    private final String dummyHash = passwords.encode(secret());
    private final AuthStore store;
    private final AuthRateLimit limits;
    private final Clock clock;

    AuthService(AuthStore store, AuthRateLimit limits, Clock clock) {
        this.store = store;
        this.limits = limits;
        this.clock = clock;
    }

    @Transactional
    public SignedIn login(AuthDtos.Login login, String previousToken) {
        String username = username(login.username());
        var user = username.matches("[a-z0-9._-]{3,100}") ? store.lockUsername(username).orElse(null) : null;
        String encoded = user == null ? null : user.passwordHash();
        boolean verified = matches(login.password(), encoded);
        if (!verified || user == null || !"ACTIVE".equals(user.status())) throw invalidLogin();

        limits.clear("login-user:" + username);
        logout(previousToken);
        return startSession(user);
    }

    @Transactional
    public SignedIn register(AuthDtos.Register request, String previousToken) {
        UUID customer = UUID.randomUUID();
        String name = request.fullName().strip();
        store.insertCustomer(customer, name, request.email(), clock.instant());
        var user = create(request.username(), name, request.email(), "CUSTOMER", request.password(), customer, null, false);
        logout(previousToken);
        return startSession(user);
    }

    public Optional<AuthDtos.Account> session(String token) {
        return validToken(token) ? store.session(hash(token), clock.instant()) : Optional.empty();
    }

    public void logout(String token) {
        if (validToken(token)) store.deleteSession(hash(token));
    }

    @Transactional
    public SignedIn changePassword(UUID accountId, AuthDtos.ChangePassword request) {
        var user = requireActive(accountId);
        if (!matches(request.currentPassword(), user.passwordHash())) {
            throw ApiException.validation("currentPassword", "Mat khau hien tai khong dung");
        }
        if (request.newPassword().equals(request.currentPassword())) {
            throw ApiException.validation("newPassword", "Mat khau moi phai khac mat khau hien tai");
        }
        store.setPassword(accountId, encode(request.newPassword()), false);
        return startSession(requireActive(accountId));
    }

    @Transactional
    public AuthDtos.RecoveryCode createRecovery(UUID accountId, AuthDtos.ConfirmPassword request) {
        var user = requireActive(accountId);
        if (user.account().mustChangePassword()) throw ApiException.forbidden("Hay doi mat khau tam truoc");
        if (!matches(request.password(), user.passwordHash())) {
            throw ApiException.validation("password", "Mat khau hien tai khong dung");
        }
        String code = secret();
        store.saveRecovery(accountId, hash(code));
        return new AuthDtos.RecoveryCode(code);
    }

    @Transactional
    public boolean resetWithRecovery(AuthDtos.Recovery request) {
        String username = username(request.username());
        var user = store.lockUsername(username).orElse(null);
        String code = request.code().strip();
        if (user == null || !"ACTIVE".equals(user.status()) || !validToken(code)
                || !store.consumeRecovery(user.account().id(), hash(code))) {
            throw ApiException.validation("code", "Ten dang nhap hoac ma khoi phuc khong hop le");
        }
        store.setPassword(user.account().id(), encode(request.newPassword()), false);
        return true;
    }

    public PageResponse<AuthDtos.StaffAccount> staff(int page, int size) { return store.staff(page, size); }

    @Transactional
    public AuthDtos.StaffCredentials createStaff(UUID adminId, AuthDtos.StaffRequest request) {
        if (!STAFF_ROLES.contains(request.role())) throw ApiException.validation("role", "Vai tro nhan vien khong hop le");
        String temporary = secret();
        var user = create(request.username(), request.fullName().strip(), request.email(), request.role(), temporary, null, adminId, true);
        return new AuthDtos.StaffCredentials(user.account(), temporary);
    }

    @Transactional
    public AuthDtos.StaffCredentials resetStaffPassword(UUID id) {
        requireStaff(id);
        String temporary = secret();
        store.setPassword(id, encode(temporary), true);
        return new AuthDtos.StaffCredentials(requireStaff(id).account(), temporary);
    }

    @Transactional
    public AuthDtos.StaffAccount setStaffLocked(UUID id, boolean locked) {
        requireStaff(id);
        store.setLocked(id, locked);
        return AuthStore.staffAccount(requireStaff(id));
    }

    @Transactional
    public void bootstrapAdmin(String name, String password) {
        if (!username(name).matches("[a-z0-9._-]{3,50}") || password.length() < 12 || password.length() > 128 || password.isBlank()) {
            throw new IllegalStateException("Invalid ANEON administrator bootstrap configuration");
        }
        var existing = store.lockUsername(username(name));
        if (existing.isPresent()) {
            if (!"SYSTEM_ADMIN".equals(existing.get().account().role())) {
                throw new IllegalStateException("ANEON bootstrap username already belongs to a non-admin account");
            }
            return;
        }
        create(name, "Quản trị ANEON", null, "SYSTEM_ADMIN", password, null, null, true);
    }

    private AuthStore.User create(String rawUsername, String name, String email, String role, String password,
                                  UUID customer, UUID creator, boolean mustChange) {
        UUID id = UUID.randomUUID();
        String username = username(rawUsername);
        try {
            store.insertAccount(id, username, name, email, role, encode(password), customer, creator, mustChange);
        } catch (DuplicateKeyException duplicate) {
            throw ApiException.conflict("Ten dang nhap da duoc su dung");
        }
        return requireActive(id);
    }

    private AuthStore.User requireActive(UUID id) {
        return store.lockAccount(id).filter(account -> "ACTIVE".equals(account.status())).orElseThrow(AuthService::invalidLogin);
    }

    private AuthStore.User requireStaff(UUID id) {
        return store.lockAccount(id).filter(user -> STAFF_ROLES.contains(user.account().role()))
                .orElseThrow(() -> ApiException.notFound("Tai khoan nhan vien", id));
    }

    private SignedIn startSession(AuthStore.User user) {
        String token = secret();
        store.insertSession(hash(token), user, clock.instant().plus(SESSION_LENGTH));
        store.loggedIn(user.account().id(), clock.instant());
        return new SignedIn(user.account(), token);
    }

    private String encode(String value) { return "{pbkdf2}" + passwords.encode(value); }

    private boolean matches(String value, String hash) {
        boolean supported = hash != null && hash.matches("\\{pbkdf2\\}[0-9a-f]{96}");
        boolean verified = passwords.matches(value, supported ? hash.substring(8) : dummyHash);
        return supported && verified;
    }

    private String secret() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String username(String username) { return username.strip().toLowerCase(Locale.ROOT); }
    private static boolean validToken(String value) { return value != null && value.matches("[A-Za-z0-9_-]{43}"); }

    static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }

    private static ApiException invalidLogin() {
        return new ApiException(ErrorCode.UNAUTHENTICATED, "Ten dang nhap hoac mat khau khong dung");
    }
}
