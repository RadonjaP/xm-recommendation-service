package com.rprelevic.xm.recom.api;

import com.rprelevic.xm.recom.api.model.CryptoStats;

import java.time.LocalDate;
import java.util.List;

/**
 * Exposes business use-case functionalities related to recommendations.
 */
public interface RecommendationService {

    /**
     * Exposes an endpoint that will return a descending sorted list of all the cryptos,
     *  comparing the normalized range (i.e. (max-min)/min). It is assumed that user will ask for last available
     *  normalized data.
     *
     * @return - a list of all the cryptos, sorted by normalized range
     */
    List<CryptoStats> listNormalized();

    /**
     * Exposes an endpoint that will return the oldest/newest/min/max values for a requested
     * crypto. It is assumed that user will ask for last available normalized data.
     *
     * @param symbol - the symbol of the crypto for which we want to find the oldest/newest/min/max
     *               values (e.g. BTC)
     * @return  - the oldest/newest/min/max values for the requested crypto
     */
    CryptoStats getCryptoStatsForSymbol(String symbol);

    /**
     * Exposes an endpoint that will return the crypto with the highest normalized range for a
     * specific day
     *
     * @param day - the day for which we want to find the crypto with the highest normalized range
     *            (e.g. 2021-01-01)
     * @return - the symbol of the crypto with the highest normalized range for the requested day
     */
    String highestNormalizedRangeForDay(LocalDate day);

}
