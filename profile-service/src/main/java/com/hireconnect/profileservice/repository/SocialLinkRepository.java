package com.hireconnect.profileservice.repository;

import com.hireconnect.profileservice.entity.SocialLink;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 * Repository interface for database operations related to SocialLink entities.
 *
 * @author Disha Gujar
 */

public interface SocialLinkRepository extends JpaRepository<SocialLink, Long> {
}
