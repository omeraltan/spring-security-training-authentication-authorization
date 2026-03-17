package com.security.training.controller;

import com.security.training.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers(Authentication authentication) {
        List<Map<String, Object>> users = userRepository.findAll().stream()
            .map(user -> Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "enabled", user.isEnabled(),
                "roles", user.getRoles().stream()
                    .map(role -> role.getName().name())
            ))
            .toList();
        return ResponseEntity.ok(Map.of(
            "users", users,
            "admin", authentication.getName(),
            "timestamp", LocalDateTime.now()));
    }

    @GetMapping("/system")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> systemInfo(Authentication authentication) {
        return ResponseEntity.ok(Map.of(
            "message", "System information",
            "access", "Admin only - Method-level security",
            "admin", authentication.getName(),
            "javaVersion", System.getProperty("java.version"),
            "osName", System.getProperty("os.name"),
            "timestamp", LocalDateTime.now()
        ));
    }

}
