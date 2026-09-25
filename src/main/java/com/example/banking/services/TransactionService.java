package com.example.banking.services;

import com.example.banking.entities.Account;
import com.example.banking.entities.Transaction;
import com.example.banking.entities.User;
import com.example.banking.repositories.AccountRepository;
import com.example.banking.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;


    @Transactional
    public Account createAccountWithInitialDeposit(User user, BigDecimal initialDeposit) {
        validateAmount(initialDeposit);
        Account account = accountService.createAccount(user, initialDeposit);
        saveTransaction(account, "INITIAL_DEPOSIT", initialDeposit, null, null, "Initial account deposit");
        return account;
    }

    @Transactional
    public void deposit(Account account, BigDecimal amount) {
        validateAmount(amount);
        Account current = accountService.getAccountById(account.getId());
        current.setBalance(current.getBalance().add(amount));
        accountRepository.save(current);
        saveTransaction(current, "DEPOSIT", amount, null, null, "Cash deposit");
    }

    @Transactional
    public void withdraw(Account account, BigDecimal amount) {
        validateAmount(amount);
        Account current = accountService.getAccountById(account.getId());
        if (current.getBalance().compareTo(amount) < 0)
            throw new IllegalArgumentException("Insufficient balance.");
        current.setBalance(current.getBalance().subtract(amount));
        accountRepository.save(current);
        saveTransaction(current, "WITHDRAW", amount, null, null, "Cash withdrawal");
    }

    @Transactional
    public void transfer(Account sender, String recipientAccountNumber, BigDecimal amount) {
        validateAmount(amount);

        Account source = accountService.getAccountById(sender.getId());
        Account destination = accountService.getByAccountNumber(recipientAccountNumber);

        if (source.getId().equals(destination.getId()))
            throw new IllegalArgumentException("Source and destination accounts must be different.");

        if (source.getBalance().compareTo(amount) < 0)
            throw new IllegalArgumentException("Insufficient balance.");

        String reference = "TRX-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        source.setBalance(source.getBalance().subtract(amount));
        destination.setBalance(destination.getBalance().add(amount));

        accountRepository.save(source);
        accountRepository.save(destination);

        String typeForSender = source.getUser().getId().equals(destination.getUser().getId())
                ? "TRANSFER_OUT" : "TRANSFER_OUT";
        String typeForReceiver = "TRANSFER_IN";

        String descriptionOut = "Transfer to " + destination.getAccountNumber();
        String descriptionIn = "Transfer from " + source.getAccountNumber();

        saveTransaction(source, typeForSender, amount, destination, reference, descriptionOut);
        saveTransaction(destination, typeForReceiver, amount, source, reference, descriptionIn);
    }

    private Transaction saveTransaction(Account account, String type, BigDecimal amount,
                                        Account relatedAccount, String reference, String description) {
        return transactionRepository.save(Transaction.builder()
                .account(account)
                .type(type)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .relatedAccount(relatedAccount)
                .transferReference(reference)
                .description(description)
                .build());
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero.");
    }
}
