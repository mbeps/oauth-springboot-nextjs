package com.maruf.oauth.repository;

import com.maruf.oauth.entity.InvalidatedToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Mongo repository for {@link InvalidatedToken} documents.
 * Exposes lookup helpers that let filters verify token revocation status.
 *
 * @author Maruf Bepary
 */
@Repository
public interface InvalidatedTokenRepository extends MongoRepository<InvalidatedToken, String> {

    /**
     * Checks if an invalidated token entry exists for the provided token string.
     * Enables constant time lookups during request filtering.
     *
     * @param token JWT value to search for in the invalidated collection
     * @author Maruf Bepary
     */
    boolean existsByToken(String token);
}