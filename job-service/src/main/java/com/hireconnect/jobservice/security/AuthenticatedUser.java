package com.hireconnect.jobservice.security;

import com.hireconnect.jobservice.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
/**
 * Domain entity or core component representing AuthenticatedUser.
 *
 * @author Disha Gujar
 */

@Getter
@AllArgsConstructor
public class AuthenticatedUser {
    private Long userId;
    private String email;
    private Role role;
}
