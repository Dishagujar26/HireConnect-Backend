package com.hireconnect.profileservice.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.util.List;

import com.hireconnect.profileservice.dto.ParsedResumeDto;
import com.hireconnect.profileservice.entity.SkillDictionary;
import com.hireconnect.profileservice.repository.SkillDictionaryRepository;
import com.hireconnect.profileservice.service.ResumeParserService;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResumeParserServiceTest {

    @Mock
    private SkillDictionaryRepository skillDictionaryRepository;

    @InjectMocks
    private ResumeParserService resumeParserService;

    @Test
    void parsePdfResume_ShouldReturnMatchedSkills() throws Exception {
        SkillDictionary javaSkill = new SkillDictionary();
        javaSkill.setSkillName("Java");

        SkillDictionary cppSkill = new SkillDictionary();
        cppSkill.setSkillName("C++");

        SkillDictionary springSkill = new SkillDictionary();
        springSkill.setSkillName("Spring Boot");

        when(skillDictionaryRepository.findAll())
                .thenReturn(List.of(javaSkill, cppSkill, springSkill));

        // Create a PDF with text containing these skills
        byte[] pdfData = createPdfWithText("I am a Java developer experienced in C++ and Spring Boot.");

        ParsedResumeDto result = resumeParserService.parsePdfResume(pdfData);

        assertNotNull(result);
        assertTrue(result.getExtractedSkills().contains("Java"));
        assertTrue(result.getExtractedSkills().contains("C++"));
        assertTrue(result.getExtractedSkills().contains("Spring Boot"));
    }

    private byte[] createPdfWithText(String text) throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);
            
            try (org.apache.pdfbox.pdmodel.PDPageContentStream contentStream = 
                    new org.apache.pdfbox.pdmodel.PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new org.apache.pdfbox.pdmodel.font.PDType1Font(
                        org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText(text);
                contentStream.endText();
            }
            
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    @Test
    void parsePdfResume_InvalidPdf_ShouldThrowException() {
        byte[] invalidPdf = "invalid pdf content".getBytes();

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> resumeParserService.parsePdfResume(invalidPdf));

        assertEquals("Could not read resume file.", exception.getMessage());
    }

    private byte[] createEmptyPdf() throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            document.addPage(new PDPage());
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }
}