package com.hireconnect.profileservice.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedResumeDto {
    private List<String> extractedSkills;
    private String suggestedHeadline;
}
