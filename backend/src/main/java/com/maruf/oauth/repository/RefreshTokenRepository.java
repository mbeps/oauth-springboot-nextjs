package com.maruf.oauth.repository;

import com.maruf.oauth.entity.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MongoDB repository for managing {@link RefreshToken} documents.
 * Provides token centric queries so services can locate records quickly.
 *
 * @author Maruf Bepary
 */
@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {

    /**
     * Finds a refresh token document by its raw token value.
     * Returns an {@link Optional} to signal when the token has expired or been deleted.
     *
     * @param token refresh token string stored in persistence
     * @author Maruf Bepary
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Deletes a refresh token document by token value.
     * Used during logout to revoke long lived credentials immediately.
     *
     * @param token refresh token string to remove from the collection
     * @author Maruf Bepary
     */
    void deleteByToken(String token);
}