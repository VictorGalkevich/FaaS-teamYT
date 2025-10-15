package com.example.domain;

import java.math.BigDecimal;

public record FreeTierParams(BigDecimal freeTierCalls, BigDecimal freeTierExecutionMs, BigDecimal freeTierMbMs,
                             BigDecimal freeTierMcpuMs) {

}
