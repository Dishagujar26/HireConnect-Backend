package com.hireconnect.auth.service;

import com.hireconnect.auth.entity.RefreshToken;
import com.hireconnect.auth.entity.UserCredential;
/**
 * Service interface defining the contract for RefreshToken business logic.
 *
 * @author Disha Gujar
 */

public interface RefreshTokenService {

    RefreshToken createOrUpdateRefreshToken(UserCredential user);

    RefreshToken verifyRefreshToken(String token);
}
