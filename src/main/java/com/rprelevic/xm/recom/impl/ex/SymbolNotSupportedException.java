package com.rprelevic.xm.recom.impl.ex;

/**
 * Exception thrown when a symbol is not supported.
 */
public class SymbolNotSupportedException extends RuntimeException {

    public SymbolNotSupportedException(String message) {
        super(message);
    }

}
