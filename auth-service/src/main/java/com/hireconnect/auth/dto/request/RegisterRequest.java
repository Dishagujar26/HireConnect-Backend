package com.hireconnect.auth.dto.request;

import com.hireconnect.auth.entity.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
/**
 * Payload object for RegisterRequest.
 *
 * @author Disha Gujar
 */

@Data
public class RegisterRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotNull
    private Role role;
}
