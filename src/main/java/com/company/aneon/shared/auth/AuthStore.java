package com.company.aneon.shared.auth;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.company.aneon.shared.web.PageResponse;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class AuthStore {
    record User(AuthDtos.Account account, String passwordHash, String status, long authVersion) { }

    private static final String ACCOUNT_QUERY = """
            select a.id, a.username, a.full_name, r.code as role, a.password_change_required,
                   a.password_hash, a.status, a.auth_version
            from tai_khoan_nguoi_dung a join vai_tro r on r.id = a.role_id
            """;
    private static final RowMapper<User> USER = (row, index) -> new User(new AuthDtos.Account(
            UUID.fromString(row.getString("id")), row.getString("username"), row.getString("full_name"),
            row.getString("role"), row.getBoolean("password_change_required")),
            row.getString("password_hash"), row.getString("status"), row.getLong("auth_version"));

    private final JdbcClient jdbc;

    AuthStore(JdbcClient jdbc) { this.jdbc = jdbc; }

    Optional<User> lockUsername(String username) {
        return jdbc.sql(ACCOUNT_QUERY + " where a.archived_at is null and r.archived_at is null and a.username = :username for update")
                .param("username", username).query(USER).optional();
    }

    Optional<User> lockAccount(UUID id) {
        return jdbc.sql(ACCOUNT_QUERY + " where a.archived_at is null and r.archived_at is null and a.id = :id for update")
                .param("id", id.toString()).query(USER).optional();
    }

    Optional<AuthDtos.Account> session(String hash, Instant now) {
        return jdbc.sql(ACCOUNT_QUERY + """
                join phien_dang_nhap s on s.account_id = a.id and s.auth_version = a.auth_version
                where s.token_hash = :hash and s.expires_at > :now and a.status = 'ACTIVE'
                  and a.archived_at is null and r.archived_at is null
                """).param("hash", hash).param("now", Timestamp.from(now)).query(USER).optional().map(User::account);
    }

    void insertSession(String hash, User user, Instant expiresAt) {
        jdbc.sql("insert into phien_dang_nhap (token_hash, account_id, auth_version, expires_at) values (:hash, :id, :version, :expiry)")
                .param("hash", hash).param("id", user.account().id().toString()).param("version", user.authVersion())
                .param("expiry", Timestamp.from(expiresAt)).update();
    }

    void deleteSession(String hash) {
        jdbc.sql("delete from phien_dang_nhap where token_hash = :hash").param("hash", hash).update();
    }

    void deleteSessions(UUID id) {
        jdbc.sql("delete from phien_dang_nhap where account_id = :id").param("id", id.toString()).update();
    }

    void loggedIn(UUID id, Instant now) {
        jdbc.sql("update tai_khoan_nguoi_dung set last_login_at = :now where id = :id")
                .param("id", id.toString()).param("now", Timestamp.from(now)).update();
    }

    void insertCustomer(UUID id, String fullName, String email, Instant now) {
        jdbc.sql("insert into khach_mua_sam (id, full_name, email, registered_at) values (:id, :name, :email, :now)")
                .param("id", id.toString()).param("name", fullName).param("email", email).param("now", Timestamp.from(now)).update();
    }

    void insertAccount(UUID id, String username, String fullName, String email, String role, String passwordHash,
                       UUID customerId, UUID createdBy, boolean mustChange) {
        jdbc.sql("""
                insert into tai_khoan_nguoi_dung (id, username, full_name, email, role_id, password_hash, status,
                    customer_id, created_by, password_change_required)
                values (:id, :username, :name, :email, (select id from vai_tro where code = :role and archived_at is null),
                    :hash, 'ACTIVE', :customer, :createdBy, :mustChange)
                """).param("id", id.toString()).param("username", username).param("name", fullName)
                .param("email", email).param("role", role).param("hash", passwordHash)
                .param("customer", customerId == null ? null : customerId.toString())
                .param("createdBy", createdBy == null ? null : createdBy.toString()).param("mustChange", mustChange).update();
    }

    void setPassword(UUID id, String hash, boolean mustChange) {
        jdbc.sql("""
                update tai_khoan_nguoi_dung set password_hash = :hash, password_change_required = :mustChange,
                    auth_version = auth_version + 1, version = version + 1, updated_at = utc_timestamp(6)
                where id = :id
                """).param("id", id.toString()).param("hash", hash).param("mustChange", mustChange).update();
        deleteSessions(id);
        clearRecovery(id);
    }

    void setLocked(UUID id, boolean locked) {
        jdbc.sql("""
                update tai_khoan_nguoi_dung set status = :status, auth_version = auth_version + 1,
                    version = version + 1, updated_at = utc_timestamp(6) where id = :id
                """).param("status", locked ? "LOCKED" : "ACTIVE").param("id", id.toString()).update();
        deleteSessions(id);
    }

    void saveRecovery(UUID id, String hash) {
        jdbc.sql("""
                insert into ma_khoi_phuc_tai_khoan (account_id, code_hash) values (:id, :hash)
                on duplicate key update code_hash = :hash
                """).param("id", id.toString()).param("hash", hash).update();
    }

    boolean consumeRecovery(UUID id, String hash) {
        return jdbc.sql("delete from ma_khoi_phuc_tai_khoan where account_id = :id and code_hash = :hash")
                .param("id", id.toString()).param("hash", hash).update() == 1;
    }

    private void clearRecovery(UUID id) {
        jdbc.sql("delete from ma_khoi_phuc_tai_khoan where account_id = :id").param("id", id.toString()).update();
    }

    PageResponse<AuthDtos.StaffAccount> staff(int page, int size) {
        String filter = " where a.archived_at is null and r.archived_at is null and r.code not in ('CUSTOMER', 'TENANT', 'SYSTEM_ADMIN')";
        long total = jdbc.sql("select count(*) from tai_khoan_nguoi_dung a join vai_tro r on r.id=a.role_id" + filter).query(Long.class).single();
        var accounts = jdbc.sql(ACCOUNT_QUERY + filter + " order by a.username, a.id limit :size offset :offset")
                .param("size", size).param("offset", (long) page * size).query(USER).list().stream().map(AuthStore::staffAccount).toList();
        return new PageResponse<>(accounts, page, size, total);
    }

    static AuthDtos.StaffAccount staffAccount(User user) {
        var a = user.account();
        return new AuthDtos.StaffAccount(a.id(), a.username(), a.fullName(), a.role(), user.status(), a.mustChangePassword());
    }
}
