package com.rprelevic.xm.recom.adtin.rest;

import com.rprelevic.xm.recom.api.RecommendationService;
import com.rprelevic.xm.recom.api.model.CryptoStats;
import com.rprelevic.xm.recom.cfg.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecommendationController.class)
@ContextConfiguration(classes = {TestSecurityConfig.class})
public class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecommendationService recommendationService;

    @Test
    void givenRequest_whenGetAllCryptosNormalizedRange_thenReturnNonEmptyList() throws Exception {
        Mockito.when(recommendationService.listNormalized())
                .thenReturn(Collections.singletonList(
                        new CryptoStats(null, null,
                                "BTC", null, 1,
                                2, 3, 4, 5)));

        mockMvc.perform(get("/api/v1/recommendation/normalized-range"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].symbol", is("BTC")))
                .andExpect(jsonPath("$[0].minRate", is(1.0)))
                .andExpect(jsonPath("$[0].maxRate", is(2.0)))
                .andExpect(jsonPath("$[0].oldestRate", is(3.0)))
                .andExpect(jsonPath("$[0].latestRate", is(4.0)));
    }

    @Test
    void givenDate_whenGetCryptoWithHighestNormalizedRangeInDay_thenReturnNonEmptyString() throws Exception {
        Mockito.when(recommendationService.highestNormalizedRangeForDay(any(LocalDate.class))).thenReturn("BTC");

        mockMvc.perform(get("/api/v1/recommendation/normalized-range/highest")
                        .param("date", "01-01-2020"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(not(emptyString()))));
    }

    @Test
    void givenSymbol_whenGetCryptoStats_thenReturnCryptoStats() throws Exception {
        CryptoStats cryptoStats = new CryptoStats(null, null, "BTC", null,
                1, 2, 3, 4, 5);
        Mockito.when(recommendationService.getCryptoStatsForSymbol(anyString()))
                .thenReturn(Optional.of(cryptoStats));

        mockMvc.perform(get("/api/v1/recommendation/stats/BTC/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol", is("BTC")))
                .andExpect(jsonPath("$.minRate", is(1.0)))
                .andExpect(jsonPath("$.maxRate", is(2.0)))
                .andExpect(jsonPath("$.oldestRate", is(3.0)))
                .andExpect(jsonPath("$.latestRate", is(4.0)));
    }

}