package com.Dotsquares.BoilerPlateAuth.Repository;

import com.Dotsquares.BoilerPlateAuth.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
