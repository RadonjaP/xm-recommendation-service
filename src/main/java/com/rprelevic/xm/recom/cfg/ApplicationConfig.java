package com.rprelevic.xm.recom.cfg;

import com.rprelevic.xm.recom.adtout.csv.DataSourceReaderCsv;
import com.rprelevic.xm.recom.adtout.h2.RatesRepositoryH2;
import com.rprelevic.xm.recom.adtout.h2.CryptoStatsRepositoryH2;
import com.rprelevic.xm.recom.adtout.h2.IngestionDetailsRepositoryH2;
import com.rprelevic.xm.recom.adtout.h2.SymbolPropertiesRepositoryH2;
import com.rprelevic.xm.recom.api.RecommendationService;
import com.rprelevic.xm.recom.impl.CryptoStatsCalculatorImpl;
import com.rprelevic.xm.recom.impl.IngestionOrchestrator;
import com.rprelevic.xm.recom.impl.RatesConsolidatorImpl;
import com.rprelevic.xm.recom.impl.RecommendationServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class ApplicationConfig {

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public RecommendationService recommendationService(JdbcTemplate jdbcTemplate) {
        return new RecommendationServiceImpl(new RatesRepositoryH2(jdbcTemplate),
                new CryptoStatsRepositoryH2(jdbcTemplate),
                new SymbolPropertiesRepositoryH2(jdbcTemplate));
    }

    @Bean
    public IngestionOrchestrator ingestionOrchestrator(JdbcTemplate jdbcTemplate) {
        return new IngestionOrchestrator(new DataSourceReaderCsv(),
                new RatesRepositoryH2(jdbcTemplate),
                new IngestionDetailsRepositoryH2(jdbcTemplate),
                new RatesConsolidatorImpl(),
                new SymbolPropertiesRepositoryH2(jdbcTemplate),
                new CryptoStatsRepositoryH2(jdbcTemplate),
                new CryptoStatsCalculatorImpl());
    }
}
