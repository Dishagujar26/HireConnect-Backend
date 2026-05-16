package com.hireconnect.interviewservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.hireconnect.interviewservice.client.dto.ApplicationSummaryDto;
import com.hireconnect.interviewservice.config.FeignClientConfig;
/**
 * Domain entity or core component representing ApplicationServiceClient.
 *
 * @author Disha Gujar
 */

@FeignClient(name = "application-service", configuration = FeignClientConfig.class)
public interface ApplicationServiceClient {

    @GetMapping("/api/applications/internal/{applicationId}")
    ApplicationSummaryDto getApplicationSummary(@PathVariable("applicationId") Long applicationId);

    @org.springframework.web.bind.annotation.PutMapping("/api/applications/{applicationId}/status")
    void updateApplicationStatus(@PathVariable("applicationId") Long applicationId, @org.springframework.web.bind.annotation.RequestBody com.hireconnect.interviewservice.client.dto.ApplicationStatusUpdateDto requestDto);
}
