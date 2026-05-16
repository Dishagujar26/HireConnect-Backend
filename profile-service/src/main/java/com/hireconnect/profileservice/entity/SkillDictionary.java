package com.hireconnect.profileservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "skill_dictionary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillDictionary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String skillName;

    // Optional category (e.g., "Frontend", "Backend", "Cloud")
    @Column(length = 100)
    private String category;
}
