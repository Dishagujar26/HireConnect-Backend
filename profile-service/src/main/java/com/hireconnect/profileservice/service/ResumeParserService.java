package com.hireconnect.profileservice.service;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import com.hireconnect.profileservice.dto.ParsedResumeDto;
import com.hireconnect.profileservice.entity.SkillDictionary;
import com.hireconnect.profileservice.repository.SkillDictionaryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeParserService {

    private final SkillDictionaryRepository skillDictionaryRepository;

    public ParsedResumeDto parsePdfResume(byte[] pdfData) {
        String extractedText = "";
        try (PDDocument document = Loader.loadPDF(pdfData)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            extractedText = pdfStripper.getText(document);
        } catch (IOException e) {
            log.error("Failed to parse PDF resume", e);
            throw new RuntimeException("Could not read resume file.");
        }

        List<String> matchedSkills = extractSkills(extractedText);
        
        return ParsedResumeDto.builder()
                .extractedSkills(matchedSkills)
                .build();
    }

    private List<String> extractSkills(String text) {
        String lowerText = text.toLowerCase();
        List<SkillDictionary> allSkills = skillDictionaryRepository.findAll();
        
        return allSkills.stream()
                .filter(skill -> {
                    String skillName = skill.getSkillName().toLowerCase();
                    // Handle special characters in skills like C++, C#, Node.js, .NET
                    // For skills that consist entirely of non-word chars or start/end with them, word boundary might fail.
                    // This is a simple regex that checks for the exact phrase with spaces around it or punctuation.
                    // To keep it robust, we'll just check if the text contains the string, but to avoid partials 
                    // like "C" in "React", we do a bit of regex if it's alphanumeric.
                    if (skillName.matches("^[a-zA-Z0-9]+$")) {
                        String regex = "\\b" + Pattern.quote(skillName) + "\\b";
                        return Pattern.compile(regex).matcher(lowerText).find();
                    } else {
                        // fallback for things like .NET, C++, Node.js
                        return lowerText.contains(skillName);
                    }
                })
                .map(SkillDictionary::getSkillName)
                .distinct()
                .collect(Collectors.toList());
    }
}
