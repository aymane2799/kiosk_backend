package com.example.kiosk.admin;

import com.example.kiosk.common.Auditable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admins", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Admin extends Auditable {

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private AdminRole role;
}
