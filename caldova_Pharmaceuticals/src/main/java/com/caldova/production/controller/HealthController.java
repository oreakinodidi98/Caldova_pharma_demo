package com.caldova.production.controller;

import com.caldova.production.repository.ProductionRepository;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    private static final Logger LOG = LoggerFactory.getLogger(HealthController.class);
    private final ProductionRepository repository;

    public HealthController(ProductionRepository repository) { this.repository = repository; }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        try {
            repository.checkConnection();
            return ResponseEntity.ok(body("UP"));
        } catch (DataAccessException ex) {
            LOG.warn("Oracle readiness check failed ({})", ex.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body("DOWN"));
        }
    }

    private Map<String, Object> body(String status) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", status);
        result.put("services", Collections.singletonMap("oracle", status));
        return result;
    }
}
