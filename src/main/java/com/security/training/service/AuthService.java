package com.security.training.service;

import com.security.training.dto.AuthResponse;
import com.security.training.dto.LoginRequest;
import com.security.training.dto.RegisterRequest;
import com.security.training.model.Role;
import com.security.training.model.User;
import com.security.training.repository.RoleRepository;
import com.security.training.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              request.getUsername(),
              request.getPassword()
          )
        );
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Set<String> roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());

        var userDetails = this.userDetailsService.loadUserByUsername(request.getUsername());
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.builder()
            .username(user.getUsername())
            .email(user.getEmail())
            .roles(roles)
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }


    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new EntityExistsException("Username already exists");
        }

        User user = User.builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))
            .email(request.getEmail())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .build();

        Role userRole = roleRepository.findByName(Role.RoleType.ROLE_USER)
            .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        Set<Role> roles = user.getRoles();
        roles.add(userRole);
        user.setRoles(roles);

        userRepository.save(user);

        return AuthResponse.builder()
            .username(user.getUsername())
            .email(user.getEmail())
            .roles(user.getRoles().stream().map(r->r.getName().name()).collect(Collectors.toSet()))
            .build();
    }


    public AuthResponse getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return AuthResponse.builder()
            .username(user.getUsername())
            .email(user.getEmail())
            .roles(authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()))
            .build();
    }

}
