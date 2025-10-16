package com.example.repository.interfaces;

import com.example.domain.ExecutionMetrics;

import java.sql.SQLException;
import java.sql.Timestamp;

public interface MetricsRepository {
    ExecutionMetrics getExecutionMetricsByName(Timestamp fromTs, Timestamp toTs, String name) throws SQLException;
}
