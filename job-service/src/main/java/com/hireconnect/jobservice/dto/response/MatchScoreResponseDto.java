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
public class MatchScoreResponseDto {
    private int score;
    private List<String> matchedSkills;
    private List<String> missingSkills;
}
