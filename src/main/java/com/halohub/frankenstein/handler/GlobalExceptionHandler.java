package com.halohub.frankenstein.handler;

import com.halohub.frankenstein.common.enums.CommonErrorCode;
import com.halohub.frankenstein.common.exception.BaseException;
import com.halohub.frankenstein.common.i18n.ErrorMessageResolver;
import com.halohub.frankenstein.common.i18n.RequestLocaleResolver;
import com.halohub.frankenstein.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final ErrorMessageResolver errorMessageResolver;

    public GlobalExceptionHandler(ErrorMessageResolver errorMessageResolver) {
        this.errorMessageResolver = errorMessageResolver;
    }

    @ExceptionHandler(BaseException.class)
    public Result<Void> handleBaseException(BaseException ex, HttpServletRequest request) {
        Locale locale = RequestLocaleResolver.resolve(request);
        String message = ex.getMessage();
        if (message == null || message.equals(ex.getErrorCode().getMessageKey())) {
            message = errorMessageResolver.resolve(ex.getErrorCode(), locale, ex.getMessageArgs());
        }
        log.info("Business exception code={}, message={}", ex.getCode(), message);
        if (ex.getCode() != null && !CommonErrorCode.UNKNOWN_ERROR.getCode().equals(ex.getCode())) {
            return Result.error(ex.getCode(), message);
        }
        return Result.error(message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.info("Parameter validation failed: {}", e.getMessage());
        Locale locale = RequestLocaleResolver.resolve(request);
        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        String errorMessage = errors.isEmpty()
                ? errorMessageResolver.resolve(CommonErrorCode.PARAM_VALIDATION_FAILED, locale)
                : errors.get(0);
        return Result.error(CommonErrorCode.PARAM_VALIDATION_FAILED.getCode(), errorMessage);
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<Void> handleSqlConstraintException(SQLIntegrityConstraintViolationException e,
                                                       HttpServletRequest request) {
        log.info("SQL constraint violation: {}", e.getMessage());
        Locale locale = RequestLocaleResolver.resolve(request);
        CommonErrorCode errorCode = CommonErrorCode.SQL_CONSTRAINT_VIOLATION;
        String message = e.getMessage();
        if (message != null) {
            if (message.contains("Duplicate entry")) {
                errorCode = CommonErrorCode.DUPLICATE_ENTRY;
            } else if (message.contains("foreign key constraint fails")) {
                errorCode = CommonErrorCode.FOREIGN_KEY_CONSTRAINT;
            }
        }
        return errorMessageResolver.toErrorResult(errorCode, locale);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNoResourceFound(NoResourceFoundException e, HttpServletRequest request) {
        log.warn("Static resource not found: {}", e.getMessage());
        Locale locale = RequestLocaleResolver.resolve(request);
        return errorMessageResolver.toErrorResult(CommonErrorCode.RESOURCE_NOT_FOUND, locale);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupported(HttpServletRequest request,
                                                 HttpRequestMethodNotSupportedException e) {
        String uri = request != null ? request.getRequestURI() : "unknown";
        String method = request != null ? request.getMethod() : "unknown";
        String supportedMethods = e.getSupportedMethods() != null
                ? String.join(",", e.getSupportedMethods())
                : "unknown";
        log.warn("HTTP method not supported: uri={}, method={}, supportedMethods={}", uri, method, supportedMethods);
        Locale locale = RequestLocaleResolver.resolve(request);
        return errorMessageResolver.toErrorResult(CommonErrorCode.REQUEST_METHOD_NOT_SUPPORTED, locale);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleGenericException(Exception e, HttpServletRequest request) {
        log.error("Unexpected exception", e);
        Locale locale = RequestLocaleResolver.resolve(request);
        return errorMessageResolver.toErrorResult(CommonErrorCode.UNKNOWN_ERROR, locale);
    }
}
