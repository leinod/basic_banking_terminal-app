package com.example.banking;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BankingApplication implements CommandLineRunner {

    private final BankingTerminal terminal;
    public BankingApplication(BankingTerminal terminal) { this.terminal = terminal; }

    public static void main(String[] args) {
        SpringApplication.run(BankingApplication.class, args);
    }

    @Override public void run(String... args) { terminal.start(); }
}
