package com.hireconnect.profileservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
/**
 * Domain entity or core component representing ApplicationServiceClient.
 *
 * @author Disha Gujar
 */

@FeignClient(name = "application-service")
public interface ApplicationServiceClient {

    @GetMapping("/api/applications/check")
    Boolean hasCandidateAppliedToJob(
            @RequestParam Long candidateId,
            @RequestParam Long jobId
    );
}
