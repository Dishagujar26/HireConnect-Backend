package com.hireconnect.applicationservice.service.impl;

import java.util.List;
import java.io.ByteArrayOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import com.hireconnect.applicationservice.client.JobServiceClient;
import com.hireconnect.applicationservice.client.ProfileServiceClient;
import com.hireconnect.applicationservice.client.dto.ApplicationSummaryDto;
import com.hireconnect.applicationservice.client.dto.CandidateProfilePreviewDto;
import com.hireconnect.applicationservice.client.dto.CandidateFullProfileForOfferDto;
import com.hireconnect.applicationservice.client.dto.JobOfferDetailsDto;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import com.hireconnect.applicationservice.dto.request.ApplicationRequestDto;
import com.hireconnect.applicationservice.dto.request.ApplicationStatusUpdateRequestDto;
import com.hireconnect.applicationservice.dto.response.ApplicationResponseDto;
import com.hireconnect.applicationservice.dto.response.RecruiterJobApplicationResponseDto;
import com.hireconnect.applicationservice.entity.JobApplication;
import com.hireconnect.applicationservice.enums.ApplicationStatus;
import com.hireconnect.applicationservice.enums.NotificationType;
import com.hireconnect.applicationservice.enums.Role;
import com.hireconnect.applicationservice.event.NotificationEvent;
import com.hireconnect.applicationservice.exception.BadRequestException;
import com.hireconnect.applicationservice.exception.ResourceNotFoundException;
import com.hireconnect.applicationservice.exception.UnauthorizedException;
import com.hireconnect.applicationservice.producer.NotificationEventProducer;
import com.hireconnect.applicationservice.repository.JobApplicationRepository;
import com.hireconnect.applicationservice.security.AuthenticatedUser;
import com.hireconnect.applicationservice.service.ApplicationService;

import lombok.RequiredArgsConstructor;

/**
 * Implementation of the ApplicationService.
 * Handles the business logic for candidate job applications, duplicate prevention,
 * status tracking, and recruiter notifications.
 * @author Disha Gujar
 */
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    private final JobApplicationRepository jobApplicationRepository;
    private final JobServiceClient jobServiceClient;
    private final NotificationEventProducer notificationEventProducer;
    private final ProfileServiceClient profileServiceClient;

    /**
     * Processes a job application submission.
     * Validates candidate role, job existence, and prevents duplicate applications.
     * 
     * @param user the authenticated candidate
     * @param requestDto the application request data
     * @return the created ApplicationResponseDto
     
 * @author Disha Gujar
 */
    @Override
    @Transactional
    @CircuitBreaker(name = "jobService", fallbackMethod = "jobServiceFallback")
    @Retry(name = "jobService")
    public ApplicationResponseDto applyToJob(AuthenticatedUser user, ApplicationRequestDto requestDto) {

        log.info("Application submission started for candidateId: {}, jobId: {}",
                user != null ? user.getUserId() : null, requestDto.getJobId());

        validateCandidate(user);

        log.info("Checking if job exists for jobId: {}", requestDto.getJobId());
        Boolean jobExists = jobServiceClient.doesJobExist(requestDto.getJobId());
        if (Boolean.FALSE.equals(jobExists)) {
            throw new ResourceNotFoundException("Job not found with id: " + requestDto.getJobId());
        }

        log.info("Checking if job is open for jobId: {}", requestDto.getJobId());
        Boolean jobOpen = jobServiceClient.isJobOpen(requestDto.getJobId());
        if (Boolean.FALSE.equals(jobOpen)) {
            throw new BadRequestException("You can only apply to open jobs");
        }

        boolean alreadyApplied = jobApplicationRepository.existsByJobIdAndCandidateId(
                requestDto.getJobId(),
                user.getUserId()
        );
        log.info("Checked duplicate application for candidateId: {}, jobId: {}, alreadyApplied: {}",
                user.getUserId(), requestDto.getJobId(), alreadyApplied);

        if (alreadyApplied) {
            throw new BadRequestException("You have already applied to this job");
        }

        log.info("Fetching recruiter id for jobId: {}", requestDto.getJobId());
        Long recruiterId = jobServiceClient.getRecruiterIdByJobId(requestDto.getJobId());

        JobApplication application = JobApplication.builder()
                .jobId(requestDto.getJobId())
                .candidateId(user.getUserId())
                .candidateEmail(user.getEmail())
                .recruiterId(recruiterId)
                .resumeUrl(requestDto.getResumeUrl())
                .coverLetter(requestDto.getCoverLetter())
                .status(ApplicationStatus.APPLIED)
                .build();

        try {
            JobApplication savedApplication = jobApplicationRepository.save(application);
            log.info("Application submitted successfully with applicationId: {}, candidateId: {}, jobId: {}",
                    savedApplication.getId(), savedApplication.getCandidateId(), savedApplication.getJobId());

            sendApplicationSubmittedNotifications(savedApplication, user);
            return mapToResponse(savedApplication);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Application submission failed due to duplicate entry for candidateId: {}, jobId: {}",
                    user.getUserId(), requestDto.getJobId());
            throw new BadRequestException("You have already applied to this job");
        }
    }

    /**
     * Fallback for Job Service failure.
     */
    public ApplicationResponseDto jobServiceFallback(AuthenticatedUser user, ApplicationRequestDto requestDto, Exception e) {
        log.error("Job Service unavailable | fallback triggered | error={}", e.getMessage());
        throw new RuntimeException("Job details could not be verified. Service temporarily unavailable.");
    }

    /**
     * Retrieves all job applications submitted by the authenticated candidate.
     * 
     * @param user the authenticated candidate
     * @return a list of ApplicationResponseDto
     
 * @author Disha Gujar
 */
    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> getMyApplications(AuthenticatedUser user) {

        log.info("Fetching applications for candidateId: {}", user != null ? user.getUserId() : null);

        validateCandidate(user);

        List<ApplicationResponseDto> applications = jobApplicationRepository
                .findByCandidateIdOrderByAppliedAtDesc(user.getUserId())
                .stream()
                .map(this::mapToResponse)
                .toList();

        log.info("Fetched {} applications for candidateId: {}", applications.size(), user.getUserId());
        return applications;
    }
    /**
     * Retrieves my application by id.
     *
     * @author Disha Gujar
     */

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponseDto getMyApplicationById(AuthenticatedUser user, Long applicationId) {

        log.info("Fetching applicationId: {} for candidateId: {}",
                applicationId, user != null ? user.getUserId() : null);

        validateCandidate(user);

        JobApplication application = jobApplicationRepository.findByIdAndCandidateId(applicationId, user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        log.info("Application found for candidateId: {}, applicationId: {}", user.getUserId(), applicationId);
        return mapToResponse(application);
    }

    /**
     * Retrieves all applications for jobs owned by the authenticated recruiter.
     * 
     * @param user the authenticated recruiter
     * @return a list of RecruiterJobApplicationResponseDto
     
 * @author Disha Gujar
 */
    @Override
    @Transactional(readOnly = true)
    @CircuitBreaker(name = "profileService", fallbackMethod = "profileServiceFallbackRecruiter")
    @Retry(name = "profileService")
    public List<RecruiterJobApplicationResponseDto> getApplicationsForRecruiter(AuthenticatedUser user) {

        log.info("Fetching all applications for recruiterId: {}", user != null ? user.getUserId() : null);

        validateRecruiter(user);

        List<Long> recruiterJobIds = jobServiceClient.getJobIdsByRecruiter(user.getUserId());
        log.info("Fetched {} job ids for recruiterId: {}",
                recruiterJobIds != null ? recruiterJobIds.size() : 0, user.getUserId());

        if (recruiterJobIds == null || recruiterJobIds.isEmpty()) {
            log.info("No jobs found for recruiterId: {}. Returning empty applications list.", user.getUserId());
            return List.of();
        }

        List<RecruiterJobApplicationResponseDto> applications = jobApplicationRepository
                .findByJobIdInOrderByAppliedAtDesc(recruiterJobIds)
                .stream()
                .map(application -> {
                    CandidateProfilePreviewDto candidateProfile = null;
                    try {
                        candidateProfile = profileServiceClient.getCandidateProfilePreview(
                                String.valueOf(user.getUserId()),
                                user.getEmail(),
                                user.getRole().name(),
                                application.getCandidateId()
                        );
                    } catch (Exception e) {
                        log.error("Failed to fetch profile preview for candidateId: {}", application.getCandidateId(), e);
                    }

                    return RecruiterJobApplicationResponseDto.builder()
                            .applicationId(application.getId())
                            .candidateId(application.getCandidateId())
                            .jobId(application.getJobId())
                            .status(application.getStatus())
                            .appliedAt(application.getAppliedAt())
                            .candidateProfile(candidateProfile)
                            .build();
                })
                .toList();

        log.info("Fetched {} applications for recruiterId: {}", applications.size(), user.getUserId());
        return applications;
    }

    /**
     * Fallback for Profile Service failure in getApplicationsForRecruiter.
     */
    public List<RecruiterJobApplicationResponseDto> profileServiceFallbackRecruiter(AuthenticatedUser user, Exception e) {
        log.error("Profile Service unavailable | fallback triggered | error={}", e.getMessage());
        List<Long> recruiterJobIds = jobServiceClient.getJobIdsByRecruiter(user.getUserId());
        if (recruiterJobIds == null || recruiterJobIds.isEmpty()) {
            return List.of();
        }
        return jobApplicationRepository.findByJobIdInOrderByAppliedAtDesc(recruiterJobIds)
                .stream()
                .map(application -> RecruiterJobApplicationResponseDto.builder()
                        .applicationId(application.getId())
                        .candidateId(application.getCandidateId())
                        .jobId(application.getJobId())
                        .status(application.getStatus())
                        .appliedAt(application.getAppliedAt())
                        .candidateProfile(null)
                        .build())
                .toList();
    }

    /**
     * Updates the status of a job application.
     * Verifies that the recruiter owns the job before updating.
     * 
     * @param user the authenticated recruiter
     * @param applicationId the ID of the application
     * @param requestDto the status update request data
     * @return the updated ApplicationResponseDto
     
 * @author Disha Gujar
 */
    @Override
    public ApplicationResponseDto updateApplicationStatus(
            AuthenticatedUser user,
            Long applicationId,
            ApplicationStatusUpdateRequestDto requestDto
    ) {

        log.info("Application status update started for userId: {}, applicationId: {}, newStatus: {}",
                user != null ? user.getUserId() : null, applicationId, requestDto.getStatus());

        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        ApplicationStatus statusToPersist = requestDto.getStatus();

        if (user.getRole() == Role.RECRUITER) {
            validateRecruiter(user);

            if (requestDto.getStatus() == ApplicationStatus.APPLIED) {
                throw new BadRequestException("Status cannot be changed back to APPLIED");
            }

            log.info("Checking ownership for recruiterId: {}, jobId: {}", user.getUserId(), application.getJobId());
            Boolean isOwner = jobServiceClient.isJobOwnedByRecruiter(application.getJobId(), user.getUserId());
            if (Boolean.FALSE.equals(isOwner)) {
                throw new UnauthorizedException("You can update applications only for your own jobs");
            }
        } // end recruiter block
        else if (user.getRole() == Role.CANDIDATE) {
            validateCandidate(user);

            if (!application.getCandidateId().equals(user.getUserId())) {
                throw new UnauthorizedException("You can only update your own applications");
            }

            // Candidates can only transition from ACCEPTED to OFFER_ACCEPTED or OFFER_REJECTED
            if (application.getStatus() != ApplicationStatus.ACCEPTED) {
                throw new BadRequestException("You can only respond to an active offer (ACCEPTED status)");
            }

            boolean candidateAcceptedOffer = requestDto.getStatus() == ApplicationStatus.OFFER_ACCEPTED
                    || requestDto.getStatus() == ApplicationStatus.ACCEPTED;
            boolean candidateRejectedOffer = requestDto.getStatus() == ApplicationStatus.OFFER_REJECTED
                    || requestDto.getStatus() == ApplicationStatus.REJECTED;

            if (!candidateAcceptedOffer && !candidateRejectedOffer) {
                throw new BadRequestException("Candidates can only ACCEPT or REJECT an offer");
            }

            statusToPersist = candidateAcceptedOffer
                    ? ApplicationStatus.OFFER_ACCEPTED
                    : ApplicationStatus.OFFER_REJECTED;

        } // end candidate block
        else {
            throw new UnauthorizedException("Invalid role for status update");
        }

        application.setStatus(statusToPersist);

        JobApplication updatedApplication;
        try {
            updatedApplication = jobApplicationRepository.save(application);
        } catch (DataIntegrityViolationException | JpaSystemException ex) {
            if (user.getRole() == Role.CANDIDATE
                    && (statusToPersist == ApplicationStatus.OFFER_ACCEPTED
                    || statusToPersist == ApplicationStatus.OFFER_REJECTED)) {
                // Backward-compatible fallback for databases that don't yet support OFFER_* enum values.
                ApplicationStatus legacyStatus = statusToPersist == ApplicationStatus.OFFER_ACCEPTED
                        ? ApplicationStatus.ACCEPTED
                        : ApplicationStatus.REJECTED;
                log.warn("OFFER_* status persistence failed for applicationId: {}. Falling back to legacy status: {}",
                        applicationId, legacyStatus);
                application.setStatus(legacyStatus);
                updatedApplication = jobApplicationRepository.save(application);
            } else {
                throw ex;
            }
        }

        log.info("Application status updated successfully for applicationId: {}, newStatus: {}",
                updatedApplication.getId(), updatedApplication.getStatus());

        sendApplicationStatusUpdateNotification(updatedApplication);
        return mapToResponse(updatedApplication);
    }
    /**
     * Retrieves application summary.
     *
     * @author Disha Gujar
     */

    @Override
    @Transactional(readOnly = true)
    public ApplicationSummaryDto getApplicationSummary(Long applicationId) {
        log.info("Fetching application summary for applicationId: {}", applicationId);

        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        log.info("Application summary prepared for applicationId: {}", application.getId());
        return ApplicationSummaryDto.builder()
                .id(application.getId())
                .jobId(application.getJobId())
                .candidateId(application.getCandidateId())
                .recruiterId(application.getRecruiterId())
                .candidateEmail(application.getCandidateEmail())
                .status(application.getStatus().name())
                .build();
    }

    private void validateCandidate(AuthenticatedUser user) {
        if (user == null || user.getRole() != Role.CANDIDATE) {
            log.warn("Unauthorized candidate action attempted. userId: {}, role: {}",
                    user != null ? user.getUserId() : null,
                    user != null ? user.getRole() : null);
            throw new UnauthorizedException("Only candidates can perform this action");
        }
    }

    private void validateRecruiter(AuthenticatedUser user) {
        if (user == null || user.getRole() != Role.RECRUITER) {
            log.warn("Unauthorized recruiter action attempted. userId: {}, role: {}",
                    user != null ? user.getUserId() : null,
                    user != null ? user.getRole() : null);
            throw new UnauthorizedException("Only recruiters can perform this action");
        }
    }

    private ApplicationResponseDto mapToResponse(JobApplication application) {
        return ApplicationResponseDto.builder()
                .id(application.getId())
                .jobId(application.getJobId())
                .candidateId(application.getCandidateId())
                .recruiterId(application.getRecruiterId())
                .status(application.getStatus())
                .resumeUrl(application.getResumeUrl())
                .coverLetter(application.getCoverLetter())
                .appliedAt(application.getAppliedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }

    private void sendApplicationSubmittedNotifications(JobApplication application, AuthenticatedUser user) {

        log.info("Sending application submitted notifications for applicationId: {}, candidateId: {}, recruiterId: {}",
                application.getId(), application.getCandidateId(), application.getRecruiterId());

        String message = "Dear Candidate,\n\n"
                + "Greetings from HireConnect.\n\n"
                + "We are pleased to inform you that your application has been successfully submitted.\n\n"
                + "Application Details:\n"
                + "Job ID: " + application.getJobId() + "\n"
                + "Application ID: " + application.getId() + "\n\n"
                + "Our recruitment team will review your profile, and you will be notified about the next steps.\n\n"
                + "We wish you the best of luck in your application.\n\n"
                + "Regards,\n"
                + "Recruitment Team\n"
                + "HireConnect";

        NotificationEvent candidateEvent = NotificationEvent.builder()
                .recipientUserId(application.getCandidateId())
                .recipientEmail(application.getCandidateEmail())
                .title("Application Submitted Successfully")
                .message(message)
                .type(NotificationType.APPLICATION)
                .sendEmail(true)
                .build();

        notificationEventProducer.sendNotification(candidateEvent);

        NotificationEvent recruiterEvent = NotificationEvent.builder()
                .recipientUserId(application.getRecruiterId())
                .recipientEmail(null)
                .title("New Application Received")
                .message("A candidate has applied for your job ID " + application.getJobId() + ".")
                .type(NotificationType.APPLICATION)
                .sendEmail(false)
                .build();

        notificationEventProducer.sendNotification(recruiterEvent);

        log.info("Application submitted notifications sent for applicationId: {}", application.getId());
    }

    private void sendApplicationStatusUpdateNotification(JobApplication application) {
        try {
            NotificationEvent statusEvent = NotificationEvent.builder()
                    .recipientUserId(application.getCandidateId())
                    .recipientEmail(null)
                    .title("Application Status Updated")
                    .message("Your application status for job ID " + application.getJobId() + " is now "
                            + application.getStatus().name() + ".")
                    .type(NotificationType.APPLICATION)
                    .sendEmail(false)
                    .build();

            notificationEventProducer.sendNotification(statusEvent);
        } catch (Exception e) {
            log.error("Notification failed: {}", e.getMessage());
        }
    }
    /**
     * Retrieves applications by job id.
     *
     * @author Disha Gujar
     */

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> getApplicationsByJobId(AuthenticatedUser user, Long jobId) {

        log.info("Fetching applications by jobId: {} for recruiterId: {}",
                jobId, user != null ? user.getUserId() : null);

        validateRecruiter(user);

        Boolean isOwner = jobServiceClient.isJobOwnedByRecruiter(jobId, user.getUserId());
        log.info("Ownership check completed for recruiterId: {}, jobId: {}, isOwner: {}",
                user.getUserId(), jobId, isOwner);

        if (Boolean.FALSE.equals(isOwner)) {
            throw new UnauthorizedException("You can view applications only for your own jobs");
        }

        List<ApplicationResponseDto> applications = jobApplicationRepository
                .findByJobIdOrderByAppliedAtDesc(jobId)
                .stream()
                .map(this::mapToResponse)
                .toList();

        log.info("Fetched {} applications for jobId: {}", applications.size(), jobId);
        return applications;
    }

    /**
     * Fetches applications for a specific job with detailed candidate profile previews.
     * 
     * @param user the authenticated recruiter
     * @param jobId the ID of the job
     * @return a list of RecruiterJobApplicationResponseDto
     
 * @author Disha Gujar
 */
    @Override
    @Transactional(readOnly = true)
    @CircuitBreaker(name = "profileService", fallbackMethod = "profileServiceFallback")
    @Retry(name = "profileService")
    public List<RecruiterJobApplicationResponseDto> getApplicationsForRecruiterJob(
            AuthenticatedUser user,
            Long jobId
    ) {
        log.info("Fetching recruiter job applications with candidate preview for recruiterId: {}, jobId: {}",
                user != null ? user.getUserId() : null, jobId);

        validateRecruiter(user);

        Boolean ownsJob = jobServiceClient.isJobOwnedByRecruiter(jobId, user.getUserId());
        log.info("Recruiter ownership check for preview completed. recruiterId: {}, jobId: {}, ownsJob: {}",
                user.getUserId(), jobId, ownsJob);

        if (ownsJob == null || !ownsJob) {
            log.warn("Recruiter is not authorized to view applications. recruiterId: {}, jobId: {}",
                    user.getUserId(), jobId);
            throw new UnauthorizedException("You are not authorized to view applications for this job");
        }

        List<RecruiterJobApplicationResponseDto> applications =
                jobApplicationRepository.findByJobIdOrderByAppliedAtDesc(jobId)
                        .stream()
                        .map(application -> {
                            CandidateProfilePreviewDto candidateProfile =
                                    profileServiceClient.getCandidateProfilePreview(
                                            String.valueOf(user.getUserId()),
                                            user.getEmail(),
                                            user.getRole().name(),
                                            application.getCandidateId()
                                    );

                            return RecruiterJobApplicationResponseDto.builder()
                                    .applicationId(application.getId())
                                    .candidateId(application.getCandidateId())
                                    .jobId(application.getJobId())
                                    .status(application.getStatus())
                                    .appliedAt(application.getAppliedAt())
                                    .candidateProfile(candidateProfile)
                                    .build();
                        })
                        .toList();

        log.info("Fetched {} recruiter job applications with candidate preview for jobId: {}",
                applications.size(), jobId);

        return applications;
    }

    /**
     * Fallback for Profile Service failure.
     */
    public List<RecruiterJobApplicationResponseDto> profileServiceFallback(AuthenticatedUser user, Long jobId, Exception e) {
        log.error("Profile Service unavailable | fallback triggered | error={}", e.getMessage());
        // Return applications without candidate preview details instead of failing the whole request
        return jobApplicationRepository.findByJobIdOrderByAppliedAtDesc(jobId)
                .stream()
                .map(application -> RecruiterJobApplicationResponseDto.builder()
                        .applicationId(application.getId())
                        .candidateId(application.getCandidateId())
                        .jobId(application.getJobId())
                        .status(application.getStatus())
                        .appliedAt(application.getAppliedAt())
                        .candidateProfile(null) // Preview is unavailable
                        .build())
                .toList();
    }
    /**
     * Checks ifs candidate applied to job.
     *
     * @author Disha Gujar
     */

    @Override
    public Boolean hasCandidateAppliedToJob(Long candidateId, Long jobId) {
        return jobApplicationRepository.existsByCandidateIdAndJobId(candidateId, jobId);
    }

    @Override
    public byte[] downloadOfferLetterPdf(
            AuthenticatedUser user,
            Long candidateId,
            Long jobId
    ) {
        validateRecruiter(user);

        if (candidateId == null || jobId == null) {
            throw new BadRequestException("candidateId and jobId are required");
        }

        Boolean ownsJob = jobServiceClient.isJobOwnedByRecruiter(jobId, user.getUserId());
        if (Boolean.FALSE.equals(ownsJob)) {
            throw new UnauthorizedException("You can download offer letters only for your own jobs");
        }

        JobApplication application = jobApplicationRepository
                .findByJobIdAndCandidateId(jobId, candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found for candidate/job"));

        // Legacy + new statuses both mean "offer accepted".
        ApplicationStatus status = application.getStatus();
        boolean offerAccepted = status == ApplicationStatus.OFFER_ACCEPTED || status == ApplicationStatus.ACCEPTED;
        if (!offerAccepted) {
            throw new BadRequestException("Offer letter can be downloaded only after offer acceptance");
        }

        CandidateFullProfileForOfferDto candidate = profileServiceClient.getCandidateFullProfileForOffer(
                String.valueOf(user.getUserId()),
                user.getEmail(),
                user.getRole().name(),
                candidateId,
                jobId
        );

        JobOfferDetailsDto job = jobServiceClient.getJobById(jobId);

        try {
            return generateOfferLetterPdf(candidate, job);
        } catch (Exception ex) {
            log.error("Failed to generate offer letter PDF | recruiterId={} | candidateId={} | jobId={}",
                    user.getUserId(), candidateId, jobId, ex);
            throw new RuntimeException("Failed to generate offer letter PDF");
        }
    }

    private byte[] generateOfferLetterPdf(CandidateFullProfileForOfferDto candidate, JobOfferDetailsDto job) throws Exception {
        // NOTE: We intentionally keep this PDF generation minimal (text + simple tables) to avoid
        // template/runtime dependencies. Browser-native "Save as PDF" is more flexible but not requested here.
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float margin = 50f;
                float pageWidth = page.getMediaBox().getWidth();
                float yStart = page.getMediaBox().getHeight() - margin;
                float y = yStart;

                // Fonts
                PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                // Header
                String today = java.time.LocalDate.now().toString();
                String company = safe(job.getCompanyName());
                String candidateName = safe(candidate.getFirstName()) + " " + safe(candidate.getLastName());

                // Company
                contentStream.beginText();
                contentStream.setFont(titleFont, 14);
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText(company);
                contentStream.endText();
                y -= 18;

                // Date (right aligned)
                contentStream.beginText();
                contentStream.setFont(bodyFont, 10);
                String dateText = "Date: " + today;
                float dateWidth = bodyFont.getStringWidth(dateText) / 1000f * 10f;
                contentStream.newLineAtOffset(pageWidth - margin - dateWidth, y);
                contentStream.showText(dateText);
                contentStream.endText();
                y -= 22;

                // Title
                contentStream.beginText();
                contentStream.setFont(titleFont, 20);
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText("OFFICIAL OFFER LETTER");
                contentStream.endText();
                y -= 28;

                // Salutation + paragraphs
                y = drawParagraph(contentStream, bodyFont, titleFont,
                        margin, y,
                        pageWidth - (margin * 2),
                        "Dear " + candidateName + ",");

                y = drawParagraph(contentStream, bodyFont, titleFont,
                        margin, y,
                        pageWidth - (margin * 2),
                        "We are pleased to offer you the position of " + safe(job.getTitle()) + " at " + company + ". " +
                                "This offer is extended based on your successful evaluation and confirmation of your interest in joining us.");

                y = drawParagraph(contentStream, bodyFont, titleFont,
                        margin, y,
                        pageWidth - (margin * 2),
                        "Please review the terms below and respond as instructed to confirm your acceptance.");

                y -= 10;

                // Key terms (simple key-value list)
                y = drawKeyValue(contentStream, bodyFont, titleFont,
                        margin, y,
                        pageWidth - (margin * 2),
                        "Role", safe(job.getTitle()));

                y = drawKeyValue(contentStream, bodyFont, titleFont,
                        margin, y,
                        pageWidth - (margin * 2),
                        "Company", company);

                y = drawKeyValue(contentStream, bodyFont, titleFont,
                        margin, y,
                        pageWidth - (margin * 2),
                        "Location", safe(job.getLocation()));

                String salaryMin = job.getSalaryMin() != null ? job.getSalaryMin().toString() : null;
                String salaryMax = job.getSalaryMax() != null ? job.getSalaryMax().toString() : null;
                String salaryText;
                if (salaryMin != null && salaryMax != null) {
                    salaryText = "INR " + salaryMin + " - INR " + salaryMax;
                } else {
                    salaryText = "Not disclosed";
                }

                String expectedCtc = candidate.getExpectedSalary() != null
                        ? "INR " + candidate.getExpectedSalary().toString()
                        : "Not disclosed";

                y = drawKeyValue(contentStream, bodyFont, titleFont,
                        margin, y,
                        pageWidth - (margin * 2),
                        "Compensation (CTC)", salaryText + " (Expected: " + expectedCtc + ")");

                String noticePeriodText;
                if (candidate.getNoticePeriodDays() == null) {
                    noticePeriodText = "Not specified";
                } else if (candidate.getNoticePeriodDays() == 0) {
                    noticePeriodText = "Immediate joiner";
                } else {
                    noticePeriodText = candidate.getNoticePeriodDays() + " days";
                }

                y = drawKeyValue(contentStream, bodyFont, titleFont,
                        margin, y,
                        pageWidth - (margin * 2),
                        "Notice Period", noticePeriodText);

                y = drawKeyValue(contentStream, bodyFont, titleFont,
                        margin, y,
                        pageWidth - (margin * 2),
                        "Work Mode", safe(candidate.getPreferredWorkMode()));

                y -= 6;

                y = drawParagraph(contentStream, bodyFont, titleFont,
                        margin, y,
                        pageWidth - (margin * 2),
                        "By accepting this offer, you agree to comply with company policies, code of conduct, and applicable employment terms.");

                // Signature
                y -= 20;
                contentStream.beginText();
                contentStream.setFont(bodyFont, 12);
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText("Sincerely,");
                contentStream.endText();
                y -= 30;

                // Signature line
                contentStream.moveTo(margin, y);
                contentStream.lineTo(margin + 260, y);
                contentStream.stroke();
                y -= 18;

                contentStream.beginText();
                contentStream.setFont(titleFont, 12);
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText("Recruitment Team");
                contentStream.endText();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private String safe(String s) {
        return s == null ? "N/A" : s;
    }

    private float drawParagraph(
            PDPageContentStream contentStream,
            PDType1Font bodyFont,
            PDType1Font titleFont,
            float x,
            float y,
            float maxWidth,
            String text
    ) throws Exception {
        // Basic wrapping by word width.
        float fontSize = 12f;
        float leading = 16f;
        List<String> lines = wrapText(text, bodyFont, fontSize, maxWidth);

        contentStream.beginText();
        contentStream.setFont(bodyFont, fontSize);
        contentStream.newLineAtOffset(x, y);
        for (String line : lines) {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -leading);
        }
        contentStream.endText();

        return y - (lines.size() * leading);
    }

    private float drawKeyValue(
            PDPageContentStream contentStream,
            PDType1Font bodyFont,
            PDType1Font titleFont,
            float x,
            float y,
            float maxWidth,
            String key,
            String value
    ) throws Exception {
        float fontSizeKey = 11f;
        float fontSizeVal = 11f;
        float leading = 14f;

        // Draw key
        contentStream.beginText();
        contentStream.setFont(titleFont, fontSizeKey);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(key + ":");
        contentStream.endText();

        // Value wrapping
        float valueX = x + 85f;
        List<String> lines = wrapText(value, bodyFont, fontSizeVal, maxWidth - (valueX - x));
        contentStream.beginText();
        contentStream.setFont(bodyFont, fontSizeVal);
        contentStream.newLineAtOffset(valueX, y);
        for (String line : lines) {
            contentStream.showText(line);
            contentStream.newLineAtOffset(0, -leading);
        }
        contentStream.endText();

        return y - (lines.size() * leading) - 8f;
    }

    private List<String> wrapText(String text, PDType1Font font, float fontSize, float maxWidth) throws Exception {
        if (text == null) {
            return List.of("");
        }
        String normalized = text.replaceAll("\\s+", " ").trim();
        if (normalized.isEmpty()) {
            return List.of("");
        }

        String[] words = normalized.split(" ");
        StringBuilder line = new StringBuilder();
        List<String> lines = new java.util.ArrayList<>();

        for (String word : words) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            float width = font.getStringWidth(candidate) / 1000f * fontSize;
            if (width <= maxWidth) {
                line.setLength(0);
                line.append(candidate);
            } else {
                if (line.length() > 0) {
                    lines.add(line.toString());
                }
                line = new StringBuilder(word);
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }
}
