package com.caldova.production.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Thymeleaf 3.1+ no longer exposes {@code #httpServletRequest} as an expression
 * utility object (removed for security reasons). Templates that need the current
 * remote user now read the {@code remoteUser} model attribute populated here.
 */
@ControllerAdvice
public class GlobalModelAttributes {
    @ModelAttribute("remoteUser")
    public String remoteUser(HttpServletRequest request) {
        return request.getRemoteUser();
    }
}
