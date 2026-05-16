package com.hireconnect.paymentservice.security;

import com.hireconnect.paymentservice.enums.Role;

/**
 * A Java Record representing the security principal of an authenticated user.
 * 
 * Records are immutable data carriers (introduced in Java 14) that eliminate boilerplate 
 * by automatically generating constructors, accessors, and utility methods. 
 * They are ideal for representing a stateless security context that should not be 
 * modified after authentication.
 * 
 * Usage Note:
 * Access fields using direct method calls instead of 'get' prefixes:
 * - user.userId() (NOT user.getUserId())
 * - user.email()  (NOT user.getEmail())
 * 
 * @author Disha Gujar
 */
public record AuthenticatedUser(Long userId, String email, Role role) {
}