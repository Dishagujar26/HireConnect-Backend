package com.hireconnect.applicationservice.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for job details used in offer letter generation.
 * This mirrors the relevant fields from the job-service response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobOfferDetailsDto {
    private Long jobId;
    private String title;
    private String companyName;
    private String location;
    private Double salaryMin;
    private Double salaryMax;
}
