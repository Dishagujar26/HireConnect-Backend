package com.hireconnect.profileservice.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MiscellaneousEntitiesTest {

    @Test
    void testEducation() {
        Education education = Education.builder()
                .institution("MIT")
                .degree("MS")
                .build();
        assertEquals("MIT", education.getInstitution());
        assertEquals("MS", education.getDegree());
    }

    @Test
    void testExperience() {
        Experience experience = Experience.builder()
                .companyName("Google")
                .jobTitle("SDE")
                .build();
        assertEquals("Google", experience.getCompanyName());
        assertEquals("SDE", experience.getJobTitle());
    }

    @Test
    void testSkill() {
        Skill skill = Skill.builder()
                .name("Java")
                .level("Advanced")
                .build();
        assertEquals("Java", skill.getName());
        assertEquals("Advanced", skill.getLevel());
    }

    @Test
    void testSocialLink() {
        SocialLink link = SocialLink.builder()
                .platform("LinkedIn")
                .url("li.com")
                .build();
        assertEquals("LinkedIn", link.getPlatform());
        assertEquals("li.com", link.getUrl());
    }

    @Test
    void testRecruiterDetail() {
        RecruiterDetail detail = RecruiterDetail.builder()
                .companyName("ACME")
                .designation("HR")
                .build();
        assertEquals("ACME", detail.getCompanyName());
        assertEquals("HR", detail.getDesignation());
    }
    
    @Test
    void testSkillDictionary() {
        SkillDictionary dict = SkillDictionary.builder()
                .skillName("Java")
                .category("Backend")
                .build();
        assertEquals("Java", dict.getSkillName());
        assertEquals("Backend", dict.getCategory());
    }
}
