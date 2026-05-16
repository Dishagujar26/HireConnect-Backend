package com.hireconnect.profileservice.repository;

import com.hireconnect.profileservice.entity.Profile;
import com.hireconnect.profileservice.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
/**
 * Repository interface for database operations related to Resume entities.
 *
 * @author Disha Gujar
 */

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    Optional<Resume> findByProfile(Profile profile);
}
