package com.example.banking.services;

import com.example.banking.entities.Account;
import com.example.banking.entities.Transaction;
import com.example.banking.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionReport {
    private final TransactionRepository transactionRepository;

    public List<Transaction> transactionHistory(Account account) {
        if (account == null || account.getId() == null)
            throw new IllegalArgumentException("Account is required.");
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(account.getId());
    }
}
