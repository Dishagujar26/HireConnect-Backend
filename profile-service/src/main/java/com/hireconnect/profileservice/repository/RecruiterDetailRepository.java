package com.hireconnect.profileservice.repository;

import com.hireconnect.profileservice.entity.RecruiterDetail;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 * Repository interface for database operations related to RecruiterDetail entities.
 *
 * @author Disha Gujar
 */

public interface RecruiterDetailRepository extends JpaRepository<RecruiterDetail, Long> {
}
