package com.hireconnect.profileservice.repository;

import com.hireconnect.profileservice.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 * Repository interface for database operations related to Skill entities.
 *
 * @author Disha Gujar
 */

public interface SkillRepository extends JpaRepository<Skill, Long> {
}
