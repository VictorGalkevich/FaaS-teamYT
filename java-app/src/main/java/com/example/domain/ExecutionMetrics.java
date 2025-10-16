package com.example.domain;

import java.math.BigDecimal;


public record ExecutionMetrics(
        BigDecimal calls,
        BigDecimal execMs,
        BigDecimal memMbMs,
        BigDecimal cpuMcpuMs,
        BigDecimal coldStarts
) {

}
