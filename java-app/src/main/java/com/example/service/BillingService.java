package com.example.service;

import com.example.domain.FreeTierParams;
import com.example.domain.ExecutionMetrics;
import com.example.domain.Invoice;
import com.example.domain.RatePlans;
import com.example.repository.interfaces.FreeTierParamsRepository;
import com.example.repository.interfaces.MetricsRepository;
import com.example.repository.interfaces.RatePlansRepository;
import org.springframework.stereotype.Component;

@Component
public class BillingService {
    private final RatePlansRepository ratePlansRepository;
    private final MetricsRepository metricsRepository;
    private final FreeTierParamsRepository freeTierParamsRepository;

    public BillingService(RatePlansRepository ratePlansRepository, MetricsRepository metricsRepository, FreeTierParamsRepository freeTierParamsRepository) {
        this.ratePlansRepository = ratePlansRepository;
        this.metricsRepository = metricsRepository;
        this.freeTierParamsRepository = freeTierParamsRepository;
    }

    public Invoice getInvoiceForRevision(String revision) {
        ExecutionMetrics metrics = metricsRepository.getExecutionMetricsByRevision(revision);
        RatePlans ratePlans = ratePlansRepository.getCurrentRatePlans();
        FreeTierParams freeTierParams = freeTierParamsRepository.getFreeTierParams();

        return InvoiceCalculator.calculateInvoice(freeTierParams, metrics, ratePlans);
    }
}
