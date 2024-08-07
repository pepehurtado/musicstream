package com.musicapp.musicstream.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.musicapp.musicstream.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
    User findByEmail(String email);
    Iterable<User> findByActive(Integer active);
    Optional<User> findByActivationToken(String activationToken);
}
