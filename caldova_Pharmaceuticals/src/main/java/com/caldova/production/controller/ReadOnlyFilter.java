package com.caldova.production.controller;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ReadOnlyFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        boolean sessionAction = "POST".equals(request.getMethod())
                && ("/login".equals(request.getServletPath()) || "/logout".equals(request.getServletPath()));
        if (!"GET".equals(request.getMethod()) && !"HEAD".equals(request.getMethod()) && !sessionAction) {
            response.setHeader("Allow", "GET, HEAD");
            response.sendError(405, "This demonstration is read-only.");
            return;
        }
        chain.doFilter(request, response);
    }
}
