package edu.ssw590.summitwealthbank.repository;

import edu.ssw590.summitwealthbank.model.CardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardTransactionRepository extends JpaRepository<CardTransaction, Long> {
    List<CardTransaction> findByAccountId(Long accountId);
}