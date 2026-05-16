package com.hireconnect.applicationservice.dto.request;

import com.hireconnect.applicationservice.enums.ApplicationStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
/**
 * Data transfer object representing ApplicationStatusUpdateRequest data.
 *
 * @author Disha Gujar
 */

@Getter
@Setter
public class ApplicationStatusUpdateRequestDto {

    @NotNull(message = "Status is required")
    private ApplicationStatus status;
}
