package edu.ssw590.summitwealthbank.service;

import edu.ssw590.summitwealthbank.model.CardTransaction;
import edu.ssw590.summitwealthbank.repository.CardTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class CardFeedService {

    private final CardTransactionRepository cardTransactionRepository;

    private static final String[] TRUSTED_MERCHANTS = {
            "Amazon", "Starbucks", "Walmart", "Apple", "Target"
    };

    private static final String[] RANDOM_MERCHANTS = {
            "Amazon", "Starbucks", "Walmart", "Apple", "Target",
            "Suspicious Crypto Exchange", "Weird Gift Shop", "TotallyNotAScam Inc"
    };

    private final Random random = new Random();

    public void generateTransaction(Long accountId) {
        String merchant = RANDOM_MERCHANTS[random.nextInt(RANDOM_MERCHANTS.length)];
        BigDecimal amount = BigDecimal.valueOf(50 + random.nextInt(1000));
        boolean isTrusted = List.of(TRUSTED_MERCHANTS).contains(merchant);
        boolean flagged = !isTrusted && amount.compareTo(BigDecimal.valueOf(500)) > 0;

        CardTransaction tx = CardTransaction.builder()
                .accountId(accountId)
                .merchant(merchant)
                .amount(amount)
                .timestamp(LocalDateTime.now())
                .flaggedFraud(flagged)
                .build();

        cardTransactionRepository.save(tx);
    }

    public List<CardTransaction> getTransactions(Long accountId) {
        return cardTransactionRepository.findByAccountId(accountId);
    }
}