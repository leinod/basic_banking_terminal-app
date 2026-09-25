package com.example.banking.services;

import com.example.banking.entities.User;
import com.example.banking.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;

    @Transactional
    public User registerUser(User user) {
        if (user.getFirstName() == null || user.getFirstName().isBlank())
            throw new IllegalArgumentException("First name is required.");
        if (user.getLastName() == null || user.getLastName().isBlank())
            throw new IllegalArgumentException("Last name is required.");
        if (user.getEmail() == null || user.getEmail().isBlank())
            throw new IllegalArgumentException("Email is required.");
        if (user.getUsername() == null || user.getUsername().isBlank())
            throw new IllegalArgumentException("Username is required.");
        if (user.getPassword() == null || user.getPassword().isBlank())
            throw new IllegalArgumentException("Password is required.");
        if (userRepository.existsByUsername(user.getUsername()))
            throw new IllegalArgumentException("Username already exists.");
        if (userRepository.existsByEmail(user.getEmail()))
            throw new IllegalArgumentException("Email already exists.");

        user.setCreatedAt(LocalDateTime.now());
        User saved = userRepository.save(user);
        authenticationService.verify(saved);
        return saved;
    }

    @Transactional
    public User updateProfile(User user, String firstName, String middleName, String lastName,
                              String email, String password) {
        if (user == null || user.getId() == null)
            throw new IllegalArgumentException("User is required.");
        if (firstName == null || firstName.isBlank())
            throw new IllegalArgumentException("First name is required.");
        if (lastName == null || lastName.isBlank())
            throw new IllegalArgumentException("Last name is required.");
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("Email is required.");
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Password is required.");

        if (!email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(email))
            throw new IllegalArgumentException("Email already exists.");

        user.setFirstName(firstName.trim());
        user.setMiddleName(middleName == null || middleName.isBlank() ? null : middleName.trim());
        user.setLastName(lastName.trim());
        user.setEmail(email.trim());
        user.setPassword(password);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User loginUser(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));
        if (!user.getPassword().equals(password))
            throw new IllegalArgumentException("Invalid username or password.");
        authenticationService.validate(user);
        return user;
    }
}
