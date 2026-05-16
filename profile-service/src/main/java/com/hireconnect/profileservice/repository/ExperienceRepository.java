package com.hireconnect.profileservice.repository;

import com.hireconnect.profileservice.entity.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 * Repository interface for database operations related to Experience entities.
 *
 * @author Disha Gujar
 */

public interface ExperienceRepository extends JpaRepository<Experience, Long> {
}
