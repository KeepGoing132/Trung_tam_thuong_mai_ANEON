package com.company.aneon.shared.error;

import java.util.List;
import java.util.Map;

/**
 * Ngoai le nghiep du co ma loi chuan. Cac loi rieng cua module ke thua lop nay
 * (VD ScheduleConflictException) de giu errorCode cu the theo baseline.
 */
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;
    private final transient List<Map.Entry<String, String>> fieldErrors;
    private final String specificCode;

    public ApiException(ErrorCode errorCode, String message) {
        this(errorCode, message, List.of(), null);
    }

    public ApiException(ErrorCode errorCode, String message, List<Map.Entry<String, String>> fieldErrors) {
        this(errorCode, message, fieldErrors, null);
    }

    /** specificCode: ma loi cu the cua module (VD SCHEDULE_CONFLICT) trong errorCode cua response. */
    public ApiException(ErrorCode errorCode, String message, List<Map.Entry<String, String>> fieldErrors,
                        String specificCode) {
        super(message);
        this.errorCode = errorCode;
        this.fieldErrors = List.copyOf(fieldErrors);
        this.specificCode = specificCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    public String specificCode() {
        return specificCode;
    }

    public List<Map.Entry<String, String>> fieldErrors() {
        return fieldErrors;
    }

    public static ApiException notFound(String resource, Object id) {
        return new ApiException(ErrorCode.RESOURCE_NOT_FOUND, resource + " khong ton tai: " + id);
    }

    public static ApiException conflict(String message) {
        return new ApiException(ErrorCode.BUSINESS_CONFLICT, message);
    }

    public static ApiException validation(String field, String message) {
        return new ApiException(ErrorCode.VALIDATION_ERROR, message,
                List.of(Map.entry(field, message)));
    }

    public static ApiException forbidden(String message) {
        return new ApiException(ErrorCode.ACCESS_DENIED, message);
    }

    public static ApiException dependencyUnavailable(String message) {
        return new ApiException(ErrorCode.DEPENDENCY_UNAVAILABLE, message);
    }
}
