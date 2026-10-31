package com.company.aneon.shared.web;

/**
 * Envelope thanh cong chuan {code, message, data} theo TK-F01/TK-F10 muc 5.
 */
public record ApiResponse<T>(int code, String message, T data) {

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(201, message, data);
    }
}
