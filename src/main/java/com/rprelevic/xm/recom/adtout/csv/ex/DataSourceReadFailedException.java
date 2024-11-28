package com.rprelevic.xm.recom.adtout.csv.ex;

public class DataSourceReadFailedException extends RuntimeException {

    public DataSourceReadFailedException(String message) {
        super(message);
    }

    public DataSourceReadFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
