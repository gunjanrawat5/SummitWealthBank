package edu.ssw590.summitwealthbank.repository;

import edu.ssw590.summitwealthbank.model.StockPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockPositionRepository extends JpaRepository<StockPosition, Long> {

    Optional<StockPosition> findByAccountIdAndStockSymbol(Long accountId, String stockSymbol);

    List<StockPosition> findByAccountId(Long accountId);

    @Query("SELECT sp FROM StockPosition sp WHERE sp.accountId IN :accountIds")
    List<StockPosition> findByAccountIds(@Param("accountIds") List<Long> accountIds);

    void deleteByAccountIdAndStockSymbolAndTotalShares(Long accountId, String stockSymbol, Long totalShares);
}
