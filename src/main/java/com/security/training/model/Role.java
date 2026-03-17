package com.security.training.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
public class Role {

    public Role(RoleType roleType, String description) {
        this.name = roleType;
        this.description = description;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RoleType name;

    private String description;

    public enum RoleType {
        ROLE_USER,
        ROLE_MANAGER,
        ROLE_ADMIN
    }

}
