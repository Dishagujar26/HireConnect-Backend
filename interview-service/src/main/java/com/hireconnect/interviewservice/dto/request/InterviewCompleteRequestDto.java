package com.hireconnect.interviewservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for completing an interview with feedback and selection action.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewCompleteRequestDto {

    @Min(1) @Max(5)
    private Integer technicalScore;

    @Min(1) @Max(5)
    private Integer communicationScore;

    @NotBlank(message = "Feedback is required to complete an interview")
    private String feedback;

    @NotNull(message = "Selection action is required")
    private SelectionAction selectionAction;

    public enum SelectionAction {
        HIRE,        // Moves application to ACCEPTED
        REJECT,      // Moves application to REJECTED
        NEXT_ROUND,  // Keeps application as SHORTLISTED
        NO_ACTION    // Just marks interview as COMPLETED
    }
}
