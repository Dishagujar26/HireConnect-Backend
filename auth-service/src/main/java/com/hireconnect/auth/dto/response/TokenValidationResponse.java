package com.hireconnect.auth.dto.response;

import com.hireconnect.auth.entity.Role;
import lombok.Builder;
import lombok.Data;
/**
 * Payload object for TokenValidationResponse.
 *
 * @author Disha Gujar
 */

@Data
@Builder
public class TokenValidationResponse {

    private boolean valid;
    private Long userId;
    private String email;
    private Role role;
    private String message;
}
