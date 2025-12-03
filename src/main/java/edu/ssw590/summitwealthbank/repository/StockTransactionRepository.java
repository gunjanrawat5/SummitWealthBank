package edu.ssw590.summitwealthbank.repository;

import edu.ssw590.summitwealthbank.model.StockTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long> {

    List<StockTransaction> findByAccountId(Long accountId);

    List<StockTransaction> findByAccountIdOrderByTimestampDesc(Long accountId);

    Optional<StockTransaction> findByTransactionReference(String transactionReference);

    @Query("SELECT st FROM StockTransaction st WHERE st.accountId IN :accountIds ORDER BY st.timestamp DESC")
    List<StockTransaction> findRecentByAccountIds(
        @Param("accountIds") List<Long> accountIds,
        Pageable pageable
    );

    @Query("SELECT st FROM StockTransaction st ORDER BY st.timestamp DESC")
    List<StockTransaction> findAllRecent(Pageable pageable);

    List<StockTransaction> findByStockSymbolOrderByTimestampDesc(String stockSymbol);
}
