package com.souflow.models.enums;

public enum OtpValidationResult {
    SUCCESS,
    EXPIRED_OR_NOT_FOUND,
    INVALID_OTP,
    MAX_ATTEMPTS_EXCEEDED;

    public static OtpValidationResult fromCode(Long code) {
        if (code == null || code == 0L) {
            return EXPIRED_OR_NOT_FOUND;
        }
        if (code == 1L) {
            return SUCCESS;
        }
        if (code == -1L) {
            return INVALID_OTP;
        }
        if (code == -2L) {
            return MAX_ATTEMPTS_EXCEEDED;
        }
        return EXPIRED_OR_NOT_FOUND;
    }
}
