package com.caldova.production.controller;

import java.security.Principal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Exposes the signed-in user name to every Thymeleaf template. Thymeleaf 3.1
 * removed the {@code #httpServletRequest} expression object used previously,
 * so the templates read this model attribute instead of the raw request.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("remoteUser")
    public String remoteUser(Principal principal) {
        return principal != null ? principal.getName() : null;
    }
}
