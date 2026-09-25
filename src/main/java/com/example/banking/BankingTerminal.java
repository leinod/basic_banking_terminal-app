package com.example.banking;

import com.example.banking.entities.Account;
import com.example.banking.entities.Transaction;
import com.example.banking.entities.User;
import com.example.banking.services.*;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

@Component
public class BankingTerminal {
    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final TransactionReport transactionReport;
    private final Scanner scanner = new Scanner(System.in);

    private User loggedInUser;
    private Account selectedAccount;

    public BankingTerminal(UserService userService, AccountService accountService,
                           TransactionService transactionService, TransactionReport transactionReport) {
        this.userService = userService;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.transactionReport = transactionReport;
    }

    public void start() {
        System.out.println("\n================================");
        System.out.println("       BASIC BANKING SYSTEM");
        System.out.println("================================");
        boolean running = true;
        while (running) running = loggedInUser == null ? authenticationMenu() : bankingMenu();
        scanner.close();
        System.exit(0);
    }

    private boolean authenticationMenu() {
        System.out.println("\n--- MAIN MENU ---");
        System.out.println("1. Register\n2. Login\n0. Exit");
        System.out.print("Choose: ");
        try {
            switch (scanner.nextLine()) {
                case "1" -> register();
                case "2" -> login();
                case "0" -> { return false; }
                default -> System.out.println("Invalid choice.");
            }
        } catch (RuntimeException e) { System.out.println("Error: " + e.getMessage()); }
        return true;
    }

    private void register() {
        System.out.println("\n--- REGISTER ---");
        System.out.print("First name: "); String first = scanner.nextLine().trim();
        System.out.print("Middle name (optional): "); String middle = scanner.nextLine().trim();
        System.out.print("Last name: "); String last = scanner.nextLine().trim();
        System.out.print("Email: "); String email = scanner.nextLine().trim();
        System.out.print("Username: "); String username = scanner.nextLine().trim();
        System.out.print("Password: "); String password = scanner.nextLine();

        User user = User.builder().firstName(first)
                .middleName(middle.isBlank() ? null : middle)
                .lastName(last).email(email).username(username).password(password).build();

        User saved = userService.registerUser(user);
        Account account = transactionService.createAccountWithInitialDeposit(saved, readAmount("Initial deposit: "));
        System.out.println("Registration successful.");
        System.out.println("Account number: " + account.getAccountNumber());
        System.out.println("Account name: " + account.getAccountName());
    }

    private void login() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Username: "); String username = scanner.nextLine().trim();
        System.out.print("Password: "); String password = scanner.nextLine();

        loggedInUser = userService.loginUser(username, password);
        List<Account> accounts = accountService.getAccountsByUser(loggedInUser);

        System.out.println("Welcome, " + fullName(loggedInUser) + "!");
        if (accounts.isEmpty()) selectedAccount = null;
        else selectAccount(accounts);
    }

    private boolean bankingMenu() {
        System.out.println("\n--- BANK MENU ---");
        System.out.println("User: " + fullName(loggedInUser));
        if (selectedAccount != null)
            System.out.println("Selected: " + selectedAccount.getAccountNumber()
                    + " | Balance: " + selectedAccount.getBalance());

        System.out.println("\n1. List accounts");
        System.out.println("2. Select account");
        System.out.println("3. Create another account");
        System.out.println("4. Check balance");
        System.out.println("5. Deposit");
        System.out.println("6. Withdraw");
        System.out.println("7. Transfer");
        System.out.println("8. Transaction history");
        System.out.println("9. Edit profile");
        System.out.println("10. Refresh");
        System.out.println("11. Logout");
        System.out.println("0. Exit");
        System.out.print("Choose: ");

        try {
            switch (scanner.nextLine()) {
                case "1" -> listAccounts();
                case "2" -> selectAccountMenu();
                case "3" -> createAnotherAccount();
                case "4" -> checkBalance();
                case "5" -> deposit();
                case "6" -> withdraw();
                case "7" -> transfer();
                case "8" -> showHistory();
                case "9" -> editProfile();
                case "10" -> refreshAccount();
                case "11" -> logout();
                case "0" -> { return false; }
                default -> System.out.println("Invalid choice.");
            }
        } catch (RuntimeException e) { System.out.println("Error: " + e.getMessage()); }
        return true;
    }

    private void listAccounts() {
        List<Account> accounts = accountService.getAccountsByUser(loggedInUser);
        System.out.println("\n--- MY ACCOUNTS ---");
        for (int i = 0; i < accounts.size(); i++) {
            Account a = accounts.get(i);
            String marker = selectedAccount != null && selectedAccount.getId().equals(a.getId()) ? " <- selected" : "";
            System.out.printf("%d. %s | %s | %.2f%s%n", i + 1, a.getAccountNumber(), a.getAccountName(), a.getBalance(), marker);
        }
    }

    private void selectAccountMenu() {
        List<Account> accounts = accountService.getAccountsByUser(loggedInUser);
        if (accounts.isEmpty()) throw new IllegalStateException("You have no accounts.");
        selectAccount(accounts);
    }

    private void selectAccount(List<Account> accounts) {
        for (int i = 0; i < accounts.size(); i++)
            System.out.printf("%d. %s | %.2f%n", i + 1, accounts.get(i).getAccountNumber(), accounts.get(i).getBalance());
        System.out.print("Select account: ");
        int choice = Integer.parseInt(scanner.nextLine());
        if (choice < 1 || choice > accounts.size()) throw new IllegalArgumentException("Invalid account selection.");
        selectedAccount = accounts.get(choice - 1);
    }

    private void createAnotherAccount() {
        selectedAccount = transactionService.createAccountWithInitialDeposit(loggedInUser, readAmount("Initial deposit: "));
        System.out.println("New account created: " + selectedAccount.getAccountNumber());
        System.out.println("Account name: " + selectedAccount.getAccountName());
        System.out.println("Initial balance: " + selectedAccount.getBalance());
    }

    private void checkBalance() {
        requireAccount(); refreshAccount();
        System.out.println("Balance: " + selectedAccount.getBalance());
    }

    private void deposit() {
        requireAccount();
        transactionService.deposit(selectedAccount, readAmount("Deposit amount: "));
        refreshAccount();
        System.out.println("Deposit successful. Balance: " + selectedAccount.getBalance());
    }

    private void withdraw() {
        requireAccount();
        transactionService.withdraw(selectedAccount, readAmount("Withdraw amount: "));
        refreshAccount();
        System.out.println("Withdrawal successful. Balance: " + selectedAccount.getBalance());
    }

    private void transfer() {
        requireAccount();
        System.out.println("\n--- TRANSFER ---");
        System.out.print("Recipient account number: ");
        String recipient = scanner.nextLine().trim();
        BigDecimal amount = readAmount("Transfer amount: ");

        transactionService.transfer(selectedAccount, recipient, amount);
        refreshAccount();

        System.out.println("Transfer successful.");
        System.out.println("Reference is recorded in transaction history.");
        System.out.println("New balance: " + selectedAccount.getBalance());
    }

    private void showHistory() {
        requireAccount();
        List<Transaction> history = transactionReport.transactionHistory(selectedAccount);
        System.out.println("\n--- TRANSACTION HISTORY ---");
        if (history.isEmpty()) { System.out.println("No transactions yet."); return; }
        for (Transaction t : history)
            System.out.printf("#%d | %-14s | %10.2f | %s | %s%n",
                    t.getId(), t.getType(), t.getAmount(), t.getCreatedAt(),
                    t.getDescription() == null ? "" : t.getDescription());
    }

    private void editProfile() {
        System.out.println("\n--- EDIT PROFILE ---");
        System.out.println("Leave a field blank to keep its current value.");
        System.out.println("Username cannot be changed because it is used for login.");
        System.out.println("Account numbers cannot be changed because they are system-generated identifiers.");

        System.out.print("First name [" + loggedInUser.getFirstName() + "]: ");
        String first = scanner.nextLine().trim();
        if (first.isBlank()) first = loggedInUser.getFirstName();

        String currentMiddle = loggedInUser.getMiddleName() == null ? "" : loggedInUser.getMiddleName();
        System.out.print("Middle name [" + currentMiddle + "]: ");
        String middle = scanner.nextLine().trim();
        if (middle.isBlank()) middle = currentMiddle;

        System.out.print("Last name [" + loggedInUser.getLastName() + "]: ");
        String last = scanner.nextLine().trim();
        if (last.isBlank()) last = loggedInUser.getLastName();

        System.out.print("Email [" + loggedInUser.getEmail() + "]: ");
        String email = scanner.nextLine().trim();
        if (email.isBlank()) email = loggedInUser.getEmail();

        System.out.print("New password [leave blank to keep current]: ");
        String password = scanner.nextLine();
        if (password.isBlank()) password = loggedInUser.getPassword();

        loggedInUser = userService.updateProfile(loggedInUser, first, middle, last, email, password);
        accountService.syncAccountNames(loggedInUser);
        if (selectedAccount != null)
            selectedAccount = accountService.getAccountById(selectedAccount.getId());

        System.out.println("Profile updated successfully.");
        System.out.println("Account names were synchronized with your updated profile.");
    }

    private void refreshAccount() {
        requireAccount();
        selectedAccount = accountService.getAccountById(selectedAccount.getId());
    }

    private void logout() {
        loggedInUser = null;
        selectedAccount = null;
        System.out.println("Logged out.");
    }

    private void requireAccount() {
        if (selectedAccount == null) throw new IllegalStateException("Please select an account first.");
    }

    private BigDecimal readAmount(String prompt) {
        System.out.print(prompt);
        BigDecimal amount = new BigDecimal(scanner.nextLine());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be greater than zero.");
        return amount;
    }

    private String fullName(User user) {
        return user.getFirstName() + " "
                + (user.getMiddleName() == null || user.getMiddleName().isBlank() ? "" : user.getMiddleName() + " ")
                + user.getLastName();
    }
}
