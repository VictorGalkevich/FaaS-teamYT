package com.example.service;

import com.example.domain.FreeTierParams;
import com.example.domain.ExecutionMetrics;
import com.example.domain.Invoice;
import com.example.domain.RatePlans;
import com.example.repository.interfaces.FreeTierParamsRepository;
import com.example.repository.interfaces.MetricsRepository;
import com.example.repository.interfaces.RatePlansRepository;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.sql.Timestamp;

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

    public Invoice getInvoiceForRevision(Timestamp fromTime, Timestamp toTime, String name) throws SQLException {
        ExecutionMetrics metrics = metricsRepository.getExecutionMetricsByName(
                fromTime,
                toTime,
                name
        );
        RatePlans ratePlans = ratePlansRepository.getRatePlans();
        FreeTierParams freeTierParams = freeTierParamsRepository.getFreeTierParams();

        return InvoiceCalculator.calculateInvoice(freeTierParams, metrics, ratePlans);
    }
}
