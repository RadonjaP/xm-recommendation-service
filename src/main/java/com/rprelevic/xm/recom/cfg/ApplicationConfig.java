package com.rprelevic.xm.recom.cfg;

import com.rprelevic.xm.recom.adtout.csv.DataSourceReaderCsv;
import com.rprelevic.xm.recom.adtout.h2.CryptoStatsRepositoryH2;
import com.rprelevic.xm.recom.adtout.h2.IngestionDetailsRepositoryH2;
import com.rprelevic.xm.recom.adtout.h2.RatesRepositoryH2;
import com.rprelevic.xm.recom.adtout.h2.SymbolPropertiesRepositoryH2;
import com.rprelevic.xm.recom.api.RecommendationService;
import com.rprelevic.xm.recom.api.repository.CryptoStatsRepository;
import com.rprelevic.xm.recom.api.repository.IngestionDetailsRepository;
import com.rprelevic.xm.recom.api.repository.RatesRepository;
import com.rprelevic.xm.recom.api.repository.SymbolPropertiesRepository;
import com.rprelevic.xm.recom.impl.CryptoStatsCalculatorImpl;
import com.rprelevic.xm.recom.impl.IngestionOrchestrator;
import com.rprelevic.xm.recom.impl.RatesConsolidatorImpl;
import com.rprelevic.xm.recom.impl.RecommendationServiceImpl;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
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
    public RatesRepository ratesRepository(JdbcTemplate jdbcTemplate) {
        return new RatesRepositoryH2(jdbcTemplate);
    }

    @Bean
    public CryptoStatsRepository cryptoStatsRepository(JdbcTemplate jdbcTemplate) {
        return new CryptoStatsRepositoryH2(jdbcTemplate);
    }

    @Bean
    public SymbolPropertiesRepository symbolPropertiesRepository(JdbcTemplate jdbcTemplate) {
        return new SymbolPropertiesRepositoryH2(jdbcTemplate);
    }

    @Bean
    public IngestionDetailsRepository ingestionDetailsRepository(JdbcTemplate jdbcTemplate) {
        return new IngestionDetailsRepositoryH2(jdbcTemplate);
    }

    @Bean
    public RecommendationService recommendationService(RatesRepository ratesRepository,
                                                       CryptoStatsRepository cryptoStatsRepository,
                                                       SymbolPropertiesRepository symbolPropertiesRepository) {
        return new RecommendationServiceImpl(ratesRepository, cryptoStatsRepository, symbolPropertiesRepository);
    }

    @Bean
    public IngestionOrchestrator ingestionOrchestrator(JdbcTemplate jdbcTemplate,
                                                       RatesRepository ratesRepository,
                                                       IngestionDetailsRepository ingestionDetailsRepository,
                                                       SymbolPropertiesRepository symbolPropertiesRepository,
                                                       CryptoStatsRepository cryptoStatsRepository) {
        return new IngestionOrchestrator(new DataSourceReaderCsv(),
                ratesRepository,
                ingestionDetailsRepository,
                new RatesConsolidatorImpl(),
                symbolPropertiesRepository,
                cryptoStatsRepository,
                new CryptoStatsCalculatorImpl());
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("cryptoStats", "highestNormalizedRangeForDay", "cryptoStatsForSymbol");
    }

    @Bean(name = "customRateLimitingFilter")
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilter() {
        FilterRegistrationBean<RateLimitingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RateLimitingFilter());
        registrationBean.addUrlPatterns("/api/*");
        return registrationBean;
    }
}
