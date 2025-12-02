package edu.ssw590.summitwealthbank.service;

import edu.ssw590.summitwealthbank.dto.TransferRequest;
import edu.ssw590.summitwealthbank.model.Account;
import edu.ssw590.summitwealthbank.model.Transaction;
import edu.ssw590.summitwealthbank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;

    public Transaction transfer(TransferRequest request, String email) {
        // Validate request
        if (request.getFromAccountId() == null || request.getToAccountId() == null) {
            throw new IllegalArgumentException("Both source and destination accounts are required");
        }

        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }

        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        Account from = accountService.getAccount(request.getFromAccountId());
        Account to = accountService.getAccount(request.getToAccountId());

        // Verify ownership - user must own the source account
        List<Account> userAccounts = accountService.getAccountsByEmail(email);
        boolean ownsFromAccount = userAccounts.stream()
                .anyMatch(acc -> acc.getId().equals(from.getId()));

        if (!ownsFromAccount) {
            throw new SecurityException("You do not have permission to transfer from this account");
        }

        if (from.isFrozen()) {
            throw new IllegalStateException("Source account is frozen. Please contact support.");
        }

        if (to.isFrozen()) {
            throw new IllegalStateException("Destination account is frozen. Transfer cannot be completed.");
        }

        if (from.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds in source account");
        }

        from.setBalance(from.getBalance().subtract(request.getAmount()));
        to.setBalance(to.getBalance().add(request.getAmount()));

        accountService.saveAccount(from);
        accountService.saveAccount(to);

        Transaction tx = Transaction.builder()
                .fromAccountId(from.getId())
                .toAccountId(to.getId())
                .amount(request.getAmount())
                .description(request.getDescription())
                .timestamp(LocalDateTime.now())
                .build();

        return transactionRepository.save(tx);
    }

    public List<Transaction> getTransactions(Long accountId) {
        return transactionRepository.findByFromAccountIdOrToAccountId(accountId, accountId);
    }

    public List<Transaction> getRecentTransactionsByEmail(String email, int limit) {
        List<Account> accounts = accountService.getAccountsByEmail(email);

        if (accounts.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> accountIds = accounts.stream()
                .map(Account::getId)
                .collect(Collectors.toList());

        return transactionRepository.findRecentByAccountIds(accountIds, PageRequest.of(0, limit));
    }
}