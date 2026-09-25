package com.example.banking.services;

import com.example.banking.entities.Account;
import com.example.banking.entities.User;
import com.example.banking.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    @Transactional
    public Account createAccount(User user, BigDecimal initialDeposit) {
        if (user == null || user.getId() == null)
            throw new IllegalArgumentException("A saved user is required.");
        if (initialDeposit == null || initialDeposit.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Initial deposit must be greater than zero.");

        Account account = Account.builder()
                .user(user)
                .accountNumber(generateAccountNumber())
                .accountName(buildFullName(user))
                .balance(initialDeposit)
                .createdAt(LocalDateTime.now())
                .build();

        return accountRepository.save(account);
    }

    @Transactional
    public void syncAccountNames(User user) {
        if (user == null || user.getId() == null)
            throw new IllegalArgumentException("User is required.");

        String fullName = buildFullName(user);
        getAccountsByUser(user).forEach(account -> account.setAccountName(fullName));
    }

    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found."));
    }

    public Account getByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Recipient account not found."));
    }

    public List<Account> getAccountsByUser(User user) {
        if (user == null || user.getId() == null)
            throw new IllegalArgumentException("User is required.");
        return accountRepository.findAllByUserIdOrderByIdAsc(user.getId());
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = "10" + ThreadLocalRandom.current().nextLong(100000000L, 1000000000L);
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }

    private String buildFullName(User user) {
        if (user.getMiddleName() == null || user.getMiddleName().isBlank())
            return user.getFirstName() + " " + user.getLastName();
        return user.getFirstName() + " " + user.getMiddleName() + " " + user.getLastName();
    }
}
