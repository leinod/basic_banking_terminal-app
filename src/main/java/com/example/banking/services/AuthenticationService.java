package com.example.banking.services;

import com.example.banking.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AccountService accountService;

    public void verify(User user) {
        if (user == null || user.getId() == null)
            throw new IllegalArgumentException("User could not be verified.");
    }

    public void validate(User user) {
        if (user == null || user.getId() == null)
            throw new IllegalArgumentException("User could not be validated.");
        accountService.getAccountsByUser(user);
    }
}
