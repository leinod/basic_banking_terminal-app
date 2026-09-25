package com.example.banking.repositories;

import com.example.banking.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findAllByUserIdOrderByIdAsc(Long userId);
    boolean existsByAccountNumber(String accountNumber);
}
