package com.musicapp.musicstream.jwt;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
    User user = userRepository.findByUsername(username);

    if (user != null) {
        log.info("userDetails: {}", user);

        // Mapea los roles a GrantedAuthority
        System.out.println("user.getRoles(): " + user.getRoles());
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
    } else {
        log.error("User not found");
        throw new UsernameNotFoundException("User not found");
    }
}


    public User getUserDetails() {
        return userDetails;
    }
}
