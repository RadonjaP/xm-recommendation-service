package com.rprelevic.xm.recom;

import com.rprelevic.xm.recom.cfg.ApplicationConfig;
import com.rprelevic.xm.recom.cfg.TestSecurityConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EnableAutoConfiguration
@ActiveProfiles("test")
@ComponentScan(basePackages = {"com.rprelevic.xm.recom.adtin.rest"})
@ContextConfiguration(classes = {TestSecurityConfig.class, ApplicationConfig.class})
class RecommendationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void setUp(@Autowired MockMvc mockMvc) throws Exception {
        mockMvc.perform(get("/api/v1/ingestion/start"))
                .andExpect(status().isOk());
    }

    @Test
    void givenRequest_whenGetAllCryptosNormalizedRange_thenReturnNonEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/recommendation/normalized-range"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].symbol", is("LTC")))
                .andExpect(jsonPath("$[0].minRate", is(103.4)))
                .andExpect(jsonPath("$[0].maxRate", is(151.5)))
                .andExpect(jsonPath("$[0].oldestRate", is(148.1)))
                .andExpect(jsonPath("$[0].latestRate", is(109.6)))
                .andExpect(jsonPath("$[1].symbol", is("BTC")))
                .andExpect(jsonPath("$[1].minRate", is(33276.59)))
                .andExpect(jsonPath("$[1].maxRate", is(47722.66)))
                .andExpect(jsonPath("$[1].oldestRate", is(46813.21)))
                .andExpect(jsonPath("$[1].latestRate", is(38415.79)));
    }

    @Test
    void givenDate_whenGetCryptoWithHighestNormalizedRangeInDay_thenReturnNonEmptyString() throws Exception {
        mockMvc.perform(get("/api/v1/recommendation/normalized-range/highest")
                        .param("date", "01-01-2022"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(not(emptyString()))));
    }

    @Test
    void givenSymbol_whenGetCryptoStats_thenReturnCryptoStats() throws Exception {
        mockMvc.perform(get("/api/v1/recommendation/stats/BTC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol", is("BTC")))
                .andExpect(jsonPath("$.minRate", is(33276.59)))
                .andExpect(jsonPath("$.maxRate", is(47722.66)))
                .andExpect(jsonPath("$.oldestRate", is(46813.21)))
                .andExpect(jsonPath("$.latestRate", is(38415.79)));
    }

    @Test
    void givenSymbolNotSupported_whenGetCryptoStats_thenReturnCryptoStats() throws Exception {
        mockMvc.perform(get("/api/v1/recommendation/stats/TEST"))
                .andExpect(status().isBadRequest());
    }

}