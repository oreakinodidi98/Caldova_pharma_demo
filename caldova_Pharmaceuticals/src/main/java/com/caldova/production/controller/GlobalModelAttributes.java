package com.caldova.production.controller;

import java.security.Principal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Exposes request-scoped attributes to all views. Thymeleaf 3.1 (bundled with
 * Spring Boot 3) removed the {@code #httpServletRequest} expression utility
 * object, so templates that previously relied on
 * {@code #httpServletRequest.remoteUser} now read the {@code remoteUser}
 * model attribute populated here instead.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("remoteUser")
    public String remoteUser(Principal principal) {
        return principal == null ? null : principal.getName();
    }
}
