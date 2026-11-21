package com.caldova.production.controller;

import com.caldova.production.service.LineNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class PageErrors {
    private static final Logger LOG = LoggerFactory.getLogger(PageErrors.class);

    @ModelAttribute("httpServletRequest")
    public HttpServletRequest httpServletRequest(HttpServletRequest request) {
        return request;
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public String invalid(Exception ex, Model model, HttpServletRequest request, HttpServletResponse response) {
        return page(400, "Invalid request", "Check the listed filters, the date, and the positive numeric line ID.",
                ex, model, request, response);
    }

    @ExceptionHandler(LineNotFoundException.class)
    public String missing(Exception ex, Model model, HttpServletRequest request, HttpServletResponse response) {
        return page(404, "Line not found", "This line is not in the recorded dataset.", ex, model, request, response);
    }

    @ExceptionHandler({DataAccessException.class, CannotCreateTransactionException.class})
    public String unavailable(Exception ex, Model model, HttpServletRequest request, HttpServletResponse response) {
        return page(503, "Database unavailable",
                "Oracle cannot be reached. Check the local database, then retry. No substitute data is shown.",
                ex, model, request, response);
    }

    private String page(int code, String title, String message, Exception ex,
                        Model model, HttpServletRequest request, HttpServletResponse response) {
        String reference = UUID.randomUUID().toString();
        LOG.warn("Request {} failed: HTTP {} ({})", reference, code, ex.getClass().getSimpleName());
        response.setStatus(code);
        model.addAttribute("code", code);
        model.addAttribute("title", title);
        model.addAttribute("message", message);
        model.addAttribute("requestId", reference);
        model.addAttribute("retryUrl", "/");
        model.addAttribute("httpServletRequest", request);
        return "error";
    }
}
