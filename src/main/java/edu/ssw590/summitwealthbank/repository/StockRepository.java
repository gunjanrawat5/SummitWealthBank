package edu.ssw590.summitwealthbank.repository;

import edu.ssw590.summitwealthbank.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    Optional<Stock> findBySymbol(String symbol);

    List<Stock> findByAvailableSharesGreaterThan(Long shares);

    List<Stock> findAllByOrderByCompanyNameAsc();

    @Query("SELECT s FROM Stock s WHERE s.availableShares > 0 ORDER BY s.symbol ASC")
    List<Stock> findAvailableStocks();
}
