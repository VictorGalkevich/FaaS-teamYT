package com.example.service;

import com.example.domain.FreeTierParams;
import com.example.domain.ExecutionMetrics;
import com.example.domain.Invoice;
import com.example.domain.RatePlans;

import java.math.BigDecimal;

public class InvoiceCalculator {
    public static Invoice calculateInvoice(FreeTierParams freeTierParams, ExecutionMetrics metrics, RatePlans ratePlans) {
        var costForCalls = ratePlans.pricePerCall().multiply(BigDecimal.ZERO.max(metrics.calls().subtract(freeTierParams.freeTierCalls())));
        var costForExecutionTime = ratePlans.pricePerMsOfExec().multiply(BigDecimal.ZERO.max(metrics.execMs().subtract(freeTierParams.freeTierExecutionMs())));
        var costForMemoryUsed = ratePlans.pricePerMbMsOfMem().multiply(BigDecimal.ZERO.max(metrics.memMbMs().subtract(freeTierParams.freeTierMbMs())));
        var costForCpuUtilization = ratePlans.pricePerMcpuMsOfCpu().multiply(BigDecimal.ZERO.max(metrics.cpuMcpuMs().subtract(freeTierParams.freeTierMcpuMs())));
        var costForColdStarts = ratePlans.coldStartFee().multiply(metrics.coldStarts());
        var finalCost = costForCalls.add(costForColdStarts).add(costForExecutionTime).add(costForCpuUtilization).add(costForMemoryUsed);
        return new Invoice(freeTierParams, ratePlans, metrics , finalCost, costForCalls, costForExecutionTime, costForMemoryUsed, costForCpuUtilization, costForColdStarts);
    }
}
