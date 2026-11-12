package com.company.aneon.shared.auth;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() { }

    public record Login(@NotBlank @Size(max = 100) String username,
                        @NotBlank @Size(max = 128) String password) { }

    public record Register(@NotBlank @Pattern(regexp = "[a-zA-Z0-9._-]{3,50}") String username,
                           @NotBlank @Size(max = 200) String fullName,
                           @Email @Size(max = 200) String email,
                           @NotBlank @Size(min = 12, max = 128) String password) { }

    public record ChangePassword(@NotBlank @Size(max = 128) String currentPassword,
                                 @NotBlank @Size(min = 12, max = 128) String newPassword) { }

    public record Recovery(@NotBlank @Size(max = 100) String username,
                           @NotBlank @Size(max = 100) String code,
                           @NotBlank @Size(min = 12, max = 128) String newPassword) { }

    public record ConfirmPassword(@NotBlank @Size(max = 128) String password) { }
    public record RecoveryCode(String code) { }

    public record StaffRequest(@NotBlank @Pattern(regexp = "[a-zA-Z0-9._-]{3,50}") String username,
                               @NotBlank @Size(max = 200) String fullName, @Email @Size(max = 200) String email,
                               @NotBlank @Size(max = 40) String role) { }
    public record SetAccountStatus(@NotNull Boolean locked) { }
    public record StaffCredentials(Account account, String temporaryPassword) { }
    public record Account(UUID id, String username, String fullName, String role, boolean mustChangePassword) { }
    public record StaffAccount(UUID id, String username, String fullName, String role, String status, boolean mustChangePassword) { }
}
