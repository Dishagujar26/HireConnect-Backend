package com.hireconnect.profileservice.config;

import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hireconnect.profileservice.repository.SkillDictionaryRepository;

@ExtendWith(MockitoExtension.class)
class SkillDictionarySeederTest {

    @Mock
    private SkillDictionaryRepository skillDictionaryRepository;

    @InjectMocks
    private SkillDictionarySeeder seeder;

    @Test
    void run_WhenEmpty_ShouldSeedSkills() throws Exception {
        when(skillDictionaryRepository.count()).thenReturn(0L);
        when(skillDictionaryRepository.findBySkillNameIgnoreCase(anyString())).thenReturn(Optional.empty());

        seeder.run();

        verify(skillDictionaryRepository, atLeastOnce()).save(any());
    }

    @Test
    void run_WhenNotEmpty_ShouldNotSeed() throws Exception {
        when(skillDictionaryRepository.count()).thenReturn(10L);

        seeder.run();

        verify(skillDictionaryRepository, never()).save(any());
    }
}
