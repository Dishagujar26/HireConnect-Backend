package com.hireconnect.jobservice.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedJobResponseDto {
    private JobResponseDto job;
    private int matchScore; // 0-100
    private List<String> matchedSkills;
    private List<String> missingSkills;
}
