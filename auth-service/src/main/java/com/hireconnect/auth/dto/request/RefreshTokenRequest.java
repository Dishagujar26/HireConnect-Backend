package com.hireconnect.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
/**
 * Payload object for RefreshTokenRequest.
 *
 * @author Disha Gujar
 */

@Data
public class RefreshTokenRequest {

    @NotBlank
    private String refreshToken;
}
