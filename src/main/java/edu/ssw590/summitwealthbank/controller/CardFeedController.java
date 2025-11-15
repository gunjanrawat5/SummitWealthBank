package edu.ssw590.summitwealthbank.controller;

import edu.ssw590.summitwealthbank.model.CardTransaction;
import edu.ssw590.summitwealthbank.service.CardFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/card-feed")
@RequiredArgsConstructor
@CrossOrigin
public class CardFeedController {

    private final CardFeedService cardFeedService;

    @PostMapping("/generate/{accountId}")
    public void generateTransaction(@PathVariable Long accountId) {
        cardFeedService.generateTransaction(accountId);
    }

    @GetMapping("/{accountId}")
    public List<CardTransaction> getTransactions(@PathVariable Long accountId) {
        return cardFeedService.getTransactions(accountId);
    }
}