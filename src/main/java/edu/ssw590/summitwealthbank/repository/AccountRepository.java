package edu.ssw590.summitwealthbank.repository;

import edu.ssw590.summitwealthbank.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserId(Long userId);
}