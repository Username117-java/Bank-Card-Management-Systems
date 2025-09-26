package com.example.bankcards.service;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public User createUser(String username, String rawPassword, String fullName, Set<Role> roles) {
        User u = User.builder()
                .username(username)
                .password(encoder.encode(rawPassword))
                .fullName(fullName)
                .roles(roles)
                .build();
        return userRepository.save(u);
    }
}
