package com.hireconnect.profileservice.entity;

import jakarta.persistence.*;
import lombok.*;
/**
 * Domain entity or core component representing RecruiterDetail.
 * Stores company and recruiter-specific professional information.
 *
 * @author Disha Gujar
 */

@Entity
@Table(name = "recruiter_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruiterDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "website")
    private String website;

    @Column(name = "company_description", columnDefinition = "TEXT")
    private String companyDescription;

    @Column(name = "designation")
    private String designation;

    // ─── Extended Company Information ────────────────────────────────────────

    @Column(name = "industry")
    private String industry;

    @Column(name = "company_size")
    private String companySize;

    @Column(name = "company_logo_url")
    private String companyLogoUrl;

    @Column(name = "company_linkedin_url")
    private String companyLinkedinUrl;

    @Column(name = "hiring_for", columnDefinition = "TEXT")
    private String hiringFor;

    @Column(name = "years_in_recruiting")
    private Integer yearsInRecruiting;

    @OneToOne
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private Profile profile;
}
