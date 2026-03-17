package com.security.training.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> reports(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
           "message","Manager reports",
           "username", authentication.getName(),
           "access", "Requires ROLE_MANAGER or ROLE_ADMIN",
           "timestamp", LocalDateTime.now()
        ));
    }

    @GetMapping("/team")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> team(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
            "message","Team management",
            "manager", authentication.getName(),
            "access", "Method-level security with @PreAuthorize",
            "timestamp", LocalDateTime.now()
        ));
    }

}
