package com.rprelevic.xm.recom;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RateLimiterIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void whenRateLimitExceeded_thenReturnTooManyRequests() throws Exception {
        // Send initial requests to approach rate limit
        final String url = "/api/v1/recommendation/normalized-range";
        for (int i = 0; i < 20; i++) {
            mockMvc.perform(get(url)).andExpect(status().isOk());
        }
        mockMvc.perform(get(url))
                .andExpect(status().isTooManyRequests());
    }
}
