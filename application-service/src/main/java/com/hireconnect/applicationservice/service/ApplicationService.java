package com.hireconnect.applicationservice.service;

import java.util.List;

import com.hireconnect.applicationservice.client.dto.ApplicationSummaryDto;
import com.hireconnect.applicationservice.dto.request.ApplicationRequestDto;
import com.hireconnect.applicationservice.dto.request.ApplicationStatusUpdateRequestDto;
import com.hireconnect.applicationservice.dto.response.ApplicationResponseDto;
import com.hireconnect.applicationservice.dto.response.RecruiterJobApplicationResponseDto;
import com.hireconnect.applicationservice.security.AuthenticatedUser;

/**
 * Service interface for job application management.
 * Defines the contract for applying to jobs, tracking applications, and updating statuses.
 * @author Disha Gujar
 */
public interface ApplicationService {

    /**
     * Submits a job application for a candidate.
     * 
     * @param user the authenticated candidate
     * @param requestDto the application request data
     * @return the created ApplicationResponseDto
     
 * @author Disha Gujar
 */
    ApplicationResponseDto applyToJob(AuthenticatedUser user, ApplicationRequestDto requestDto);

    /**
     * Retrieves all applications submitted by the authenticated candidate.
     * 
     * @param user the authenticated candidate
     * @return a list of ApplicationResponseDto
     
 * @author Disha Gujar
 */
    List<ApplicationResponseDto> getMyApplications(AuthenticatedUser user);

    /**
     * Retrieves a specific application by its ID for the authenticated candidate.
     * 
     * @param user the authenticated candidate
     * @param applicationId the ID of the application
     * @return the ApplicationResponseDto
     
 * @author Disha Gujar
 */
    ApplicationResponseDto getMyApplicationById(AuthenticatedUser user, Long applicationId);

    /**
     * Retrieves all applications relevant to the authenticated recruiter.
     * 
     * @param user the authenticated recruiter
     * @return a list of RecruiterJobApplicationResponseDto
     
 * @author Disha Gujar
 */
    List<RecruiterJobApplicationResponseDto> getApplicationsForRecruiter(AuthenticatedUser user);

    /**
     * Updates the status of a job application.
     * 
     * @param user the authenticated recruiter
     * @param applicationId the ID of the application
     * @param requestDto the status update request data
     * @return the updated ApplicationResponseDto
     
 * @author Disha Gujar
 */
    ApplicationResponseDto updateApplicationStatus(
            AuthenticatedUser user,
            Long applicationId,
            ApplicationStatusUpdateRequestDto requestDto
    );

    /**
     * Retrieves an internal summary of an application.
     * 
     * @param applicationId the ID of the application
     * @return the ApplicationSummaryDto
     
 * @author Disha Gujar
 */
    ApplicationSummaryDto getApplicationSummary(Long applicationId);
    
    /**
     * Retrieves all applications for a specific job ID.
     * 
     * @param user the authenticated recruiter
     * @param jobId the ID of the job
     * @return a list of ApplicationResponseDto
     
 * @author Disha Gujar
 */
    List<ApplicationResponseDto> getApplicationsByJobId(AuthenticatedUser user, Long jobId);
    
    /**
     * Retrieves applications for a recruiter's job with candidate profile details.
     * 
     * @param user the authenticated recruiter
     * @param jobId the ID of the job
     * @return a list of RecruiterJobApplicationResponseDto
     
 * @author Disha Gujar
 */
    List<RecruiterJobApplicationResponseDto> getApplicationsForRecruiterJob(
            AuthenticatedUser user,
            Long jobId
    );

    /**
     * Checks if a candidate has already applied to a specific job.
     * 
     * @param candidateId the ID of the candidate
     * @param jobId the ID of the job
     * @return true if applied, false otherwise
     
 * @author Disha Gujar
 */
    Boolean hasCandidateAppliedToJob(Long candidateId, Long jobId);

    /**
     * Generates and returns the candidate offer letter as a PDF.
     * Only allowed for recruiters who own the job, and only after offer acceptance.
     */
    byte[] downloadOfferLetterPdf(AuthenticatedUser user, Long candidateId, Long jobId);
}