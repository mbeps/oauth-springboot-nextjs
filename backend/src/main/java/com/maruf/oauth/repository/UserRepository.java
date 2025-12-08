package com.maruf.oauth.repository;

import com.maruf.oauth.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Mongo repository for {@link com.maruf.oauth.entity.User} documents.
 *
 * @author Maruf Bepary
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    /**
     * Finds a user by email for login and signup checks.
     *
     * @param email unique email address
     * @return optional user when found
     * @author Maruf Bepary
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Checks whether an account exists for the given email.
     *
     * @param email unique email address
     * @return {@code true} if a user record exists
     * @author Maruf Bepary
     */
    boolean existsByEmail(String email);
}
