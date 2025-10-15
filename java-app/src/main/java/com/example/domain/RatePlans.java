package com.example.domain;

import java.math.BigDecimal;

public record RatePlans(BigDecimal pricePerCall, BigDecimal pricePerMsOfExec, BigDecimal pricePerMbMsOfMem,
                        BigDecimal pricePerMcpuMsOfCpu, BigDecimal coldStartFee) {

}
