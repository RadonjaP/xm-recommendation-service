package com.rprelevic.xm.recom.adtin.rest;

import com.rprelevic.xm.recom.api.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendation")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/normalized-range")
    @Operation(summary = "Fetch descending sorted list of all cryptos comparing normalized range")
    @ApiResponse(responseCode = "200", description = "Success")
    public ResponseEntity<List<CryptoStatsRs>> getAllCryptosNormalizedRange() {
        return ResponseEntity.ok(recommendationService.listNormalized()
                .stream().map(CryptoStatsRs::fromCryptoStats)
                .toList());
    }

    @GetMapping("/normalized-range/highest")
    @Operation(summary = "Get the crypto with the highest normalized range for a specific day")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "400", description = "Invalid date format")
    public ResponseEntity<String> getCryptoWithHighestNormalizedRangeInDay(
            @Parameter(name = "date", description = "Specific day in format dd-MM-yyyy", example = "01-01-2020", required = true)
            @RequestParam String date) {

        final LocalDate day;
        try {
            day = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid date format. Please use dd-MM-yyyy");
        }

        return ResponseEntity.ok(recommendationService.highestNormalizedRangeForDay(day));
    }

    @GetMapping("/stats/{symbol}/info")
    @Operation(summary = "Get the oldest/newest/min/max values for a requested crypto")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "404", description = "Crypto not found")
    public ResponseEntity<CryptoStatsRs> getCryptoStats(
            @Parameter(name = "symbol", description = "Symbol of the crypto", required = true)
            @PathVariable String symbol) {

        final var cryptoStats = recommendationService.getCryptoStatsForSymbol(symbol);
        return cryptoStats.map(stats -> ResponseEntity.ok(CryptoStatsRs.fromCryptoStats(stats)))
                .orElseGet(() -> ResponseEntity.status(404).build());
    }
}