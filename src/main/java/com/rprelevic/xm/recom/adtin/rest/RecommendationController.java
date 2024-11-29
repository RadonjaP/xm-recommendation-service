package com.rprelevic.xm.recom.adtin.rest;

import com.rprelevic.xm.recom.api.RecommendationService;
import com.rprelevic.xm.recom.api.model.CryptoStats;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController("/api/v1/recommendation")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/normalized-range")
    @Operation(summary = "Fetch descending sorted list of all cryptos comparing normalized range")
    @ApiResponse(responseCode = "200", description = "Success")
    public ResponseEntity<List<CryptoStats>> getAllCryptosNormalizedRange() {

        return ResponseEntity.ok(recommendationService.listNormalized());
    }

    @GetMapping("/normalized-range/highest")
    @Operation(summary = "Get the crypto with the highest normalized range for a specific day")
    @Parameter(name = "date", description = "Specific day in format dd-mm-yyyy", example = "01-01-2020", required = true)
    @ApiResponse(responseCode = "200", description = "Success")
    public ResponseEntity<String> getCryptoWithHighestNormalizedRangeInDay(@RequestParam String date) {

        final LocalDate day;
        try {
            day = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid date format. Please use dd-MM-yyyy");
        }

        return ResponseEntity.ok(recommendationService.highestNormalizedRangeForDay(day));
    }

    @GetMapping("/stats/{symbol}")
    @Operation(summary = "Get the oldest/newest/min/max values for a requested crypto")
    @Parameter(name = "symbol", description = "Symbol of the crypto", required = true)
    @ApiResponse(responseCode = "200", description = "Success")
    public ResponseEntity<CryptoStatsRs> getCryptoStats(@PathVariable String symbol) {

        final var cryptoStats = recommendationService.getCryptoStatsForSymbol(symbol);
        return ResponseEntity.ok(CryptoStatsRs.fromCryptoStats(cryptoStats));
    }

}
