package com.publicissuetracker.repository;

import com.publicissuetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    // find a user by email (used for login & uniqueness checks)
    Optional<User> findByEmail(String email);

    // check if a user exists with this email
    boolean existsByEmail(String email);
}
