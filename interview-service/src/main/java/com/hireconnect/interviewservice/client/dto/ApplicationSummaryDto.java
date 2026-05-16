package com.hireconnect.interviewservice.client.dto;

import lombok.Getter;
import lombok.Setter;
/**
 * Data transfer object representing ApplicationSummary data.
 *
 * @author Disha Gujar
 */

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationSummaryDto {
    private Long id;
    private Long jobId;
    private Long candidateId;
    private String candidateEmail;
    private Long recruiterId;
    private String status;
}
