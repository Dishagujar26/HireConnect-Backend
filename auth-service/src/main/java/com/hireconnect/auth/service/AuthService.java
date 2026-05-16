package com.hireconnect.auth.service;

import com.hireconnect.auth.dto.request.ForgotPasswordRequest;
import com.hireconnect.auth.dto.request.LoginRequest;
import com.hireconnect.auth.dto.request.RefreshTokenRequest;
import com.hireconnect.auth.dto.request.RegisterRequest;
import com.hireconnect.auth.dto.request.ResetPasswordRequest;
import com.hireconnect.auth.dto.response.AuthResponse;
import com.hireconnect.auth.dto.response.TokenValidationResponse;

/**
 * Service interface for authentication operations.
 * Defines the contract for user management, token handling, and password recovery.
 * @author Disha Gujar
 */
public interface AuthService {

    /**
     * Registers a new user.
     * 
     * @param request the registration request
     * @return the authentication response with tokens
     
 * @author Disha Gujar
 */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates a user with email and password.
     * 
     * @param request the login request
     * @return the authentication response with tokens
     
 * @author Disha Gujar
 */
    AuthResponse login(LoginRequest request);

    /**
     * Refreshes the access token using a refresh token.
     * 
     * @param request the refresh token request
     * @return the authentication response with a new access token
     
 * @author Disha Gujar
 */
    AuthResponse refreshToken(RefreshTokenRequest request);
    
    /**
     * Initiates the forgot password process.
     * 
     * @param request the forgot password request
     
 * @author Disha Gujar
 */
    void forgotPassword(ForgotPasswordRequest request);

    /**
     * Resets the user's password.
     * 
     * @param request the reset password request
     
 * @author Disha Gujar
 */
    void resetPassword(ResetPasswordRequest request);

    /**
     * Validates a JWT token.
     * 
     * @param token the token string (including Bearer prefix)
     * @return the token validation response
     
 * @author Disha Gujar
 */
    TokenValidationResponse validateToken(String token);
}