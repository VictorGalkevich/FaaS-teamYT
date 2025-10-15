package com.example.repository.interfaces;

import com.example.domain.ExecutionMetrics;

public interface MetricsRepository {
    ExecutionMetrics getExecutionMetricsByRevision(String revision);
}
