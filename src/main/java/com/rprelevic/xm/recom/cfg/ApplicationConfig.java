package com.rprelevic.xm.recom.cfg;

import com.rprelevic.xm.recom.adtout.csv.DataSourceReaderCsv;
import com.rprelevic.xm.recom.adtout.h2.CryptoRepositoryH2;
import com.rprelevic.xm.recom.adtout.h2.CryptoStatsRepositoryH2;
import com.rprelevic.xm.recom.adtout.h2.IngestionDetailsRepositoryH2;
import com.rprelevic.xm.recom.adtout.h2.SymbolPropertiesRepositoryH2;
import com.rprelevic.xm.recom.api.RecommendationService;
import com.rprelevic.xm.recom.impl.CryptoStatsCalculatorImpl;
import com.rprelevic.xm.recom.impl.IngestionOrchestrator;
import com.rprelevic.xm.recom.impl.PriceConsolidatorImpl;
import com.rprelevic.xm.recom.impl.RecommendationServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ApplicationConfig {

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate();
    }

    @Bean
    public RecommendationService recommendationService(JdbcTemplate jdbcTemplate) {
        return new RecommendationServiceImpl(new CryptoRepositoryH2(jdbcTemplate),
                new CryptoStatsRepositoryH2(jdbcTemplate),
                new SymbolPropertiesRepositoryH2(jdbcTemplate));
    }

    @Bean
    public IngestionOrchestrator ingestionOrchestrator(JdbcTemplate jdbcTemplate) {
        return new IngestionOrchestrator(new DataSourceReaderCsv(),
                new CryptoRepositoryH2(jdbcTemplate),
                new IngestionDetailsRepositoryH2(jdbcTemplate),
                new PriceConsolidatorImpl(),
                new SymbolPropertiesRepositoryH2(jdbcTemplate),
                new CryptoStatsRepositoryH2(jdbcTemplate),
                new CryptoStatsCalculatorImpl());
    }
}
