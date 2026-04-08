package com.classgo.backend.infrastructure.security;

import com.classgo.backend.domain.model.User;
import com.classgo.backend.domain.repository.UserRepository;
import com.classgo.backend.shared.exception.ResourceNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class ClassGoUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public ClassGoUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByEmailIgnoreCase(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new AuthUser(user.getId(), user.getEmail(), user.getRole(), user.getPasswordHash(), user.isActive());
    }
}
