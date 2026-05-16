package com.hireconnect.applicationservice.client.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Minimal candidate profile used to generate the offer letter PDF.
 */
@Getter
@Setter
@Builder
public class CandidateFullProfileForOfferDto {
    private Long userId;
    private String firstName;
    private String lastName;

    private Integer noticePeriodDays;
    private Long expectedSalary;
    private String preferredWorkMode;

    // Optional extras for letter personalization
    private BigDecimal totalExperienceYears;
}

