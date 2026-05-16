package com.hireconnect.profileservice.security;

import com.hireconnect.profileservice.entity.Role;
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
    private Role role;
}
