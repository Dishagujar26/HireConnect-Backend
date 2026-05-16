package com.hireconnect.profileservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hireconnect.profileservice.entity.SkillDictionary;

import java.util.Optional;

@Repository
public interface SkillDictionaryRepository extends JpaRepository<SkillDictionary, Long> {
    Optional<SkillDictionary> findBySkillNameIgnoreCase(String skillName);
}
