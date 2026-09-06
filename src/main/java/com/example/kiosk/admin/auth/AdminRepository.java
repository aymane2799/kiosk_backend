package com.example.kiosk.admin.auth;

import com.example.kiosk.admin.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin,String> {
    Optional<Admin> findByEmail(String email);
}
