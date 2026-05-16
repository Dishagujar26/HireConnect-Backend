package com.hireconnect.profileservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
/**
 * Data transfer object representing RecruiterDetailRequest data.
 *
 * @author Disha Gujar
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruiterDetailRequestDto {

    private String companyName;
    private String website;
    private String companyDescription;
    private String designation;

    // ─── Extended Company Information ────────────────────────────────────────
    private String industry;
    private String companySize;
    private String companyLogoUrl;
    private String companyLinkedinUrl;
    private String hiringFor;
    private Integer yearsInRecruiting;
}

