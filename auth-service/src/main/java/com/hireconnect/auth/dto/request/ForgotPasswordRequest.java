package com.hireconnect.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
/**
 * Payload object for ForgotPasswordRequest.
 *
 * @author Disha Gujar
 */

@Data
public class ForgotPasswordRequest {

    @Email
    @NotBlank
    private String email;
}
