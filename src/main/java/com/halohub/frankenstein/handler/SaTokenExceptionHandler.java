package com.halohub.frankenstein.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.halohub.frankenstein.common.enums.CommonErrorCode;
import com.halohub.frankenstein.common.i18n.ErrorMessageResolver;
import com.halohub.frankenstein.common.i18n.RequestLocaleResolver;
import com.halohub.frankenstein.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

@RestControllerAdvice
@Order(0)
@Slf4j
public class SaTokenExceptionHandler {

    private final ErrorMessageResolver errorMessageResolver;

    public SaTokenExceptionHandler(ErrorMessageResolver errorMessageResolver) {
        this.errorMessageResolver = errorMessageResolver;
    }

    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleNotLogin(NotLoginException e, HttpServletRequest request,
                                       HttpServletResponse response) {
        Locale locale = RequestLocaleResolver.resolve(request);
        log.info("Not logged in: type={}, message={}", e.getType(), e.getMessage());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return errorMessageResolver.toErrorResult(CommonErrorCode.USER_NOT_LOGIN, locale);
    }

    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleNotRole(NotRoleException e, HttpServletRequest request,
                                      HttpServletResponse response) {
        Locale locale = RequestLocaleResolver.resolve(request);
        log.info("Role denied: role={}, message={}", e.getRole(), e.getMessage());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        return errorMessageResolver.toErrorResult(CommonErrorCode.UNAUTHORIZED_OPERATION, locale);
    }

    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleNotPermission(NotPermissionException e, HttpServletRequest request,
                                            HttpServletResponse response) {
        Locale locale = RequestLocaleResolver.resolve(request);
        log.info("Permission denied: permission={}, message={}", e.getPermission(), e.getMessage());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        return errorMessageResolver.toErrorResult(CommonErrorCode.UNAUTHORIZED_OPERATION, locale);
    }
}
