package edu.ssw590.summitwealthbank.repository;

import edu.ssw590.summitwealthbank.model.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByFromAccountIdOrToAccountId(Long fromAccountId, Long toAccountId);

    @Query("SELECT t FROM Transaction t WHERE t.fromAccountId IN :accountIds OR t.toAccountId IN :accountIds ORDER BY t.timestamp DESC")
    List<Transaction> findRecentByAccountIds(@Param("accountIds") List<Long> accountIds, Pageable pageable);

    @Query("SELECT t FROM Transaction t ORDER BY t.timestamp DESC")
    List<Transaction> findAllRecent(Pageable pageable);

    Optional<Transaction> findByTransactionReference(String transactionReference);
}