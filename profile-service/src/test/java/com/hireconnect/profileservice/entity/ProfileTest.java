package com.hireconnect.profileservice.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProfileTest {

    @Test
    void onCreate_ShouldSetTimestamps() {
        Profile profile = new Profile();
        profile.onCreate();
        
        assertNotNull(profile.getCreatedAt());
        assertNotNull(profile.getUpdatedAt());
    }

    @Test
    void onUpdate_ShouldUpdateTimestamp() throws InterruptedException {
        Profile profile = new Profile();
        profile.onCreate();
        java.time.LocalDateTime firstUpdate = profile.getUpdatedAt();
        
        Thread.sleep(10);
        profile.onUpdate();
        
        assertTrue(profile.getUpdatedAt().isAfter(firstUpdate));
    }

    @Test
    void builder_ShouldSetFields() {
        Profile profile = Profile.builder()
                .userId(1L)
                .firstName("John")
                .role(Role.CANDIDATE)
                .build();
        
        assertEquals(1L, profile.getUserId());
        assertEquals("John", profile.getFirstName());
        assertEquals(Role.CANDIDATE, profile.getRole());
    }
}
