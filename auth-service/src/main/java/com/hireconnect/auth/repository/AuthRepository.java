package com.hireconnect.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hireconnect.auth.entity.Role;
import com.hireconnect.auth.entity.UserCredential;
/**
 * Repository interface for database operations related to Auth entities.
 *
 * @author Disha Gujar
 */

public interface AuthRepository extends JpaRepository<UserCredential, Long> {

    Optional<UserCredential> findByEmail(String email);

    boolean existsByEmail(String email);

    // Admin: statistics queries
    long countByRole(Role role);

    long countByIsActive(boolean isActive);

    // Admin: list users by role
    List<UserCredential> findByRole(Role role);
}
