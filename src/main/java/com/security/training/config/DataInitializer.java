package com.security.training.config;

import com.security.training.model.Role;
import com.security.training.model.User;
import com.security.training.repository.RoleRepository;
import com.security.training.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (roleRepository.count() > 0) {
                log.info("Data already exists, skipping initialization.");
                return;
            }

            // Create roles
            Role roleUser = roleRepository.save(new Role(Role.RoleType.ROLE_USER, "Standard user role"));
            Role roleManager = roleRepository.save(new Role(Role.RoleType.ROLE_MANAGER, "Manager role"));
            Role roleAdmin = roleRepository.save(new Role(Role.RoleType.ROLE_ADMIN, "System administrator role"));
            log.info("Roles created: ROLE_USER, ROLE_MANAGER, ROLE_ADMIN");

            // Admin user
            User admin = new User("admin", "admin123", "admin@example.com", "Admin", "User");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEnabled(true);
            admin.setAccountExpired(false);
            admin.setAccountLocked(false);
            admin.setCredentialsExpired(false);
            admin.setRoles(Set.of(roleAdmin));
            userRepository.save(admin);
            log.info("Admin user created: admin / admin123");

            // Manager user
            User manager = new User("manager", "manager123", "manager@example.com", "Manager", "User");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setEnabled(true);
            manager.setAccountExpired(false);
            manager.setAccountLocked(false);
            manager.setCredentialsExpired(false);
            manager.setRoles(Set.of(roleManager, roleUser));
            userRepository.save(manager);
            log.info("Manager user created: manager / manager123");

            // Standard user
            User user = new User("user", "user123", "user@example.com", "Normal", "User");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setEnabled(true);
            user.setAccountExpired(false);
            user.setAccountLocked(false);
            user.setCredentialsExpired(false);
            user.setRoles(Set.of(roleUser));
            userRepository.save(user);
            log.info("Standard user created: user / user123");

            log.info("Data initialization completed.");
        };
    }
}
