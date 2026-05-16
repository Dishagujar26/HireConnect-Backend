package com.hireconnect.applicationservice.security;

import com.hireconnect.applicationservice.enums.Role;

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
