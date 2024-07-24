package com.musicapp.musicstream.jwt;

import java.util.ArrayList;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.musicapp.musicstream.entities.User;
import com.musicapp.musicstream.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CustomerDetailsService implements UserDetailsService{

    @Autowired
    private UserRepository userRepository;

    private User userDetails;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("username: {}", username);
        userDetails = userRepository.findByUsername(username);

        if (!Objects.isNull(userDetails)) {
            log.info("userDetails: {}", userDetails);
            return new org.springframework.security.core.userdetails.User(userDetails.getUsername(), userDetails.getPassword(), new ArrayList<>());
        } else {
            log.error("User not found");
            throw new UsernameNotFoundException("User not found");
        }
    }

    public User getUserDetails() {
        return userDetails;
    }
}
