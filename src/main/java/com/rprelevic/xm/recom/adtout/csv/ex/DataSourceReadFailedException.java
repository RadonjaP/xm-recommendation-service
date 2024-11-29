package com.rprelevic.xm.recom.adtout.csv.ex;

/**
 * Exception thrown when reading data from a data source fails.
 */
public class DataSourceReadFailedException extends RuntimeException {

    public DataSourceReadFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
