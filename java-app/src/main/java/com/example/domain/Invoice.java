package com.example.domain;

import java.math.BigDecimal;

public record Invoice(FreeTierParams freeTierParams, RatePlans ratePlans, ExecutionMetrics metrics,
                      BigDecimal finalCost, BigDecimal costForCalls, BigDecimal costForExecutionTime,
                      BigDecimal costForMemoryUsed, BigDecimal costForCpuUtilization, BigDecimal costForColdStarts) {

}
