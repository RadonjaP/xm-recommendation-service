package com.rprelevic.xm.recom.adtin.rest;

import com.rprelevic.xm.recom.cfg.TestSecurityConfig;
import com.rprelevic.xm.recom.impl.IngestionOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IngestionController.class)
@ContextConfiguration(classes = {TestSecurityConfig.class})
class IngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IngestionOrchestrator ingestionOrchestrator;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void testStartIngestion() throws Exception {
        // Mock the behavior of ingestionOrchestrator
        doNothing().when(ingestionOrchestrator).ingest(any());

        // Perform the GET request and expect a 200 OK status
        mockMvc.perform(get("/api/v1/ingestion/start"))
                .andExpect(status().isOk());

        // Verify that the ingestionOrchestrator.ingest method was called
        Mockito.verify(ingestionOrchestrator, Mockito.atLeastOnce()).ingest(any());
    }
}