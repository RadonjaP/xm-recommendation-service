package com.rprelevic.xm.recom.api.model;

/**
 * Represents the status of the data.
 * <p>
 * RED: Dataset was missing or had conflicts during stats calculation.
 * AMBER: Partial dataset was available during stats calculation.
 * GREEN: Full dataset was available during stats calculation.
 */
public enum DataStatus {
    RED, AMBER, GREEN
}
