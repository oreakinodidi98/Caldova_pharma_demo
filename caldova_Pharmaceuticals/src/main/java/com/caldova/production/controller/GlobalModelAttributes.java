package com.caldova.production.controller;

import java.security.Principal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Exposes the signed-in user's name to every Thymeleaf view as {@code principalName}.
 * Replaces the removed Thymeleaf 3.1 {@code #httpServletRequest} expression utility object.
 */
@ControllerAdvice
public class GlobalModelAttributes {
    @ModelAttribute("principalName")
    public String principalName(Principal principal) {
        return principal == null ? null : principal.getName();
    }
}
