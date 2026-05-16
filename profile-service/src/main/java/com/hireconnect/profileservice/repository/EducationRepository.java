package com.hireconnect.profileservice.repository;

import com.hireconnect.profileservice.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 * Repository interface for database operations related to Education entities.
 *
 * @author Disha Gujar
 */

public interface EducationRepository extends JpaRepository<Education, Long> {
}
