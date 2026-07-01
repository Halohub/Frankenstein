package com.halohub.frankenstein.common.enums;

import lombok.Getter;

/**
 * Common error codes for typical web application scenarios.
 * <p>
 * Message text is stored in {@code i18n/error_messages*.properties} and resolved by language.
 * <p>
 * Code ranges:
 * <ul>
 *   <li>1xxx: account</li>
 *   <li>2xxx: parameter validation</li>
 *   <li>3xxx: business logic</li>
 *   <li>4xxx: data operations</li>
 *   <li>5xxx: file operations</li>
 *   <li>7xxx: other</li>
 * </ul>
 */
@Getter
public enum CommonErrorCode implements ErrorCode {

    ACCOUNT_NOT_FOUND(1001),
    PASSWORD_ERROR(1006),
    USER_NOT_LOGIN(1010),
    ACCOUNT_ALREADY_LOGIN(1005),
    ACCOUNT_KICKED(1012),
    ACCOUNT_LOCKED(1014),

    PARAM_EXCEPTION(2001),
    PARAM_CANNOT_BE_EMPTY(2002),
    PARAM_VALIDATION_FAILED(2003),

    BUSINESS_EXCEPTION(3001),
    NO_CORRESPONDING_DATA(3002),
    CODE_EXPIRED(3012),
    VERIFY_CODE_ERROR(3015),

    DATA_INSERT_FAILED(4001),
    DELETION_NOT_ALLOWED(4002),
    NAME_ALREADY_EXIST(4003),
    STATUS_ERROR(4008),

    FILE_UPLOAD_FAILED(5001),
    FILE_TOO_LARGE(5010),

    UNKNOWN_ERROR(7001),
    OPERATION_FAILED(7002),
    RESOURCE_NOT_FOUND(7007),
    SQL_CONSTRAINT_VIOLATION(7008),
    DUPLICATE_ENTRY(7009),
    FOREIGN_KEY_CONSTRAINT(7010),
    JSON_CONVERSION_ERROR(7011),
    EMAIL_SEND_FAILED(7012),
    RATE_LIMIT_EXCEEDED(7013),
    UNAUTHORIZED_OPERATION(7020),
    REQUEST_METHOD_NOT_SUPPORTED(7021);

    private final Integer code;

    CommonErrorCode(Integer code) {
        this.code = code;
    }

    @Override
    public String getMessageKey() {
        return name();
    }

    public static CommonErrorCode getByCode(Integer code) {
        if (code == null) {
            return UNKNOWN_ERROR;
        }
        for (CommonErrorCode errorCode : values()) {
            if (errorCode.code.equals(code)) {
                return errorCode;
            }
        }
        return UNKNOWN_ERROR;
    }
}
