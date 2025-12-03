package edu.ssw590.summitwealthbank.config;

import edu.ssw590.summitwealthbank.model.Stock;
import edu.ssw590.summitwealthbank.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockDataInitializer implements CommandLineRunner {

    private final StockRepository stockRepository;

    @Value("${app.initialize-stocks:true}")
    private boolean initializeStocks;

    @Override
    public void run(String... args) {
        if (!initializeStocks) {
            log.info("Stock initialization is disabled");
            return;
        }

        if (stockRepository.count() > 0) {
            log.info("Stocks already exist in database, skipping initialization");
            return;
        }

        log.info("Initializing stock data with 50 NASDAQ companies...");

        List<Stock> stocks = new ArrayList<>();

        // Technology Sector
        stocks.add(createStock("AAPL", "Apple Inc.", 175.50, 10000, "Technology", "American multinational technology company"));
        stocks.add(createStock("MSFT", "Microsoft Corporation", 380.25, 10000, "Technology", "American multinational technology corporation"));
        stocks.add(createStock("GOOGL", "Alphabet Inc.", 142.50, 10000, "Technology", "American multinational technology conglomerate"));
        stocks.add(createStock("AMZN", "Amazon.com Inc.", 155.75, 10000, "Consumer Cyclical", "American multinational technology and e-commerce company"));
        stocks.add(createStock("META", "Meta Platforms Inc.", 485.20, 10000, "Technology", "American multinational technology company - Social Media"));
        stocks.add(createStock("NVDA", "NVIDIA Corporation", 495.00, 10000, "Technology", "American multinational technology company - GPUs"));
        stocks.add(createStock("TSLA", "Tesla Inc.", 245.00, 10000, "Consumer Cyclical", "American electric vehicle and clean energy company"));
        stocks.add(createStock("NFLX", "Netflix Inc.", 485.50, 10000, "Communication Services", "American subscription streaming service"));
        stocks.add(createStock("ADBE", "Adobe Inc.", 560.75, 10000, "Technology", "American multinational computer software company"));
        stocks.add(createStock("CRM", "Salesforce Inc.", 275.30, 10000, "Technology", "American cloud-based software company"));
        stocks.add(createStock("INTC", "Intel Corporation", 42.85, 15000, "Technology", "American multinational corporation and technology company"));
        stocks.add(createStock("AMD", "Advanced Micro Devices", 125.40, 12000, "Technology", "American multinational semiconductor company"));
        stocks.add(createStock("ORCL", "Oracle Corporation", 115.60, 12000, "Technology", "American multinational computer technology corporation"));
        stocks.add(createStock("CSCO", "Cisco Systems Inc.", 52.75, 15000, "Technology", "American multinational technology conglomerate"));
        stocks.add(createStock("AVGO", "Broadcom Inc.", 1350.00, 5000, "Technology", "American designer, developer and global supplier of semiconductors"));

        // Consumer & Retail
        stocks.add(createStock("SBUX", "Starbucks Corporation", 98.75, 15000, "Consumer Cyclical", "American multinational chain of coffeehouses"));
        stocks.add(createStock("COST", "Costco Wholesale Corp", 720.50, 8000, "Consumer Defensive", "American multinational corporation - Retail"));
        stocks.add(createStock("BKNG", "Booking Holdings Inc.", 3650.00, 3000, "Consumer Cyclical", "American travel technology company"));
        stocks.add(createStock("ABNB", "Airbnb Inc.", 145.25, 12000, "Consumer Cyclical", "American vacation rental online marketplace company"));
        stocks.add(createStock("EBAY", "eBay Inc.", 48.90, 15000, "Consumer Cyclical", "American multinational e-commerce company"));

        // Healthcare & Biotech
        stocks.add(createStock("AMGN", "Amgen Inc.", 285.40, 10000, "Healthcare", "American multinational biopharmaceutical company"));
        stocks.add(createStock("GILD", "Gilead Sciences Inc.", 78.25, 12000, "Healthcare", "American biopharmaceutical company"));
        stocks.add(createStock("VRTX", "Vertex Pharmaceuticals", 425.80, 8000, "Healthcare", "American biopharmaceutical company"));
        stocks.add(createStock("REGN", "Regeneron Pharmaceuticals", 895.50, 7000, "Healthcare", "American biotechnology company"));
        stocks.add(createStock("BIIB", "Biogen Inc.", 245.30, 10000, "Healthcare", "American multinational biotechnology company"));
        stocks.add(createStock("MRNA", "Moderna Inc.", 95.40, 12000, "Healthcare", "American pharmaceutical and biotechnology company"));
        stocks.add(createStock("ILMN", "Illumina Inc.", 142.75, 10000, "Healthcare", "American biotechnology company"));

        // Financial Services
        stocks.add(createStock("PYPL", "PayPal Holdings Inc.", 62.85, 15000, "Financial Services", "American multinational financial technology company"));
        stocks.add(createStock("ADSK", "Autodesk Inc.", 255.40, 10000, "Technology", "American multinational software corporation"));

        // Communication & Media
        stocks.add(createStock("CMCSA", "Comcast Corporation", 42.50, 15000, "Communication Services", "American telecommunications conglomerate"));
        stocks.add(createStock("ATVI", "Activision Blizzard", 95.25, 12000, "Communication Services", "American video game holding company"));
        stocks.add(createStock("EA", "Electronic Arts Inc.", 138.90, 10000, "Communication Services", "American video game company"));

        // Semiconductor & Hardware
        stocks.add(createStock("QCOM", "Qualcomm Inc.", 165.75, 12000, "Technology", "American multinational semiconductor and telecommunications equipment company"));
        stocks.add(createStock("TXN", "Texas Instruments Inc.", 175.20, 10000, "Technology", "American technology company that designs and manufactures semiconductors"));
        stocks.add(createStock("AMAT", "Applied Materials Inc.", 185.50, 10000, "Technology", "American corporation that supplies equipment to semiconductor industry"));
        stocks.add(createStock("LRCX", "Lam Research Corp", 825.30, 7000, "Technology", "American supplier of wafer fabrication equipment"));
        stocks.add(createStock("KLAC", "KLA Corporation", 615.75, 8000, "Technology", "American capital equipment company"));
        stocks.add(createStock("MCHP", "Microchip Technology", 88.40, 12000, "Technology", "American manufacturer of microcontroller products"));

        // Software & Cloud
        stocks.add(createStock("WDAY", "Workday Inc.", 245.60, 10000, "Technology", "American on‑demand financial management and human capital management software"));
        stocks.add(createStock("PANW", "Palo Alto Networks", 315.25, 10000, "Technology", "American multinational cybersecurity company"));
        stocks.add(createStock("SNPS", "Synopsys Inc.", 545.80, 8000, "Technology", "American electronic design automation company"));
        stocks.add(createStock("CDNS", "Cadence Design Systems", 285.40, 10000, "Technology", "American computational software company"));
        stocks.add(createStock("ANSS", "ANSYS Inc.", 325.75, 10000, "Technology", "American company that develops and markets CAE/multiphysics engineering simulation software"));
        stocks.add(createStock("DDOG", "Datadog Inc.", 125.90, 12000, "Technology", "American monitoring and analytics platform for developers"));
        stocks.add(createStock("TEAM", "Atlassian Corporation", 215.40, 10000, "Technology", "Australian software company that develops products for software developers"));
        stocks.add(createStock("ZM", "Zoom Video Communications", 70.25, 15000, "Technology", "American communications technology company"));

        // Industrial & Transportation
        stocks.add(createStock("PCAR", "PACCAR Inc.", 105.80, 12000, "Industrials", "American truck manufacturer"));

        // Food & Beverage
        stocks.add(createStock("MDLZ", "Mondelez International", 72.50, 15000, "Consumer Defensive", "American multinational confectionery, food, and beverage conglomerate"));
        stocks.add(createStock("PEP", "PepsiCo Inc.", 175.30, 10000, "Consumer Defensive", "American multinational food and beverage corporation"));

        // Energy
        stocks.add(createStock("XEL", "Xcel Energy Inc.", 62.40, 15000, "Utilities", "American utility company based in Minneapolis, Minnesota"));

        stockRepository.saveAll(stocks);

        log.info("Successfully initialized {} stocks", stocks.size());
    }

    private Stock createStock(String symbol, String companyName, double currentPrice, long totalShares, String sector, String description) {
        return Stock.builder()
                .symbol(symbol)
                .companyName(companyName)
                .currentPrice(BigDecimal.valueOf(currentPrice))
                .totalShares(totalShares)
                .availableShares(totalShares)
                .sector(sector)
                .description(description)
                .build();
    }
}
