package com.example.repository.impl;

import com.example.domain.ExecutionMetrics;
import com.example.repository.interfaces.MetricsRepository;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class MetricsRepositoryImpl implements MetricsRepository {

    private final DataSource dataSource;

    public MetricsRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public ExecutionMetrics getExecutionMetricsByRevision(String revision) {
        final String sql = """
            SELECT calls, exec_ms, mem_mb_ms, cpu_mcpu_ms, cold_start
            FROM execution_metrics
            WHERE revision = ?
            """;
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, revision);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("No execution_metrics for revision=" + revision);
                }
                return new ExecutionMetrics(
                        rs.getBigDecimal("calls"),
                        rs.getBigDecimal("exec_ms"),
                        rs.getBigDecimal("mem_mb_ms"),
                        rs.getBigDecimal("cpu_mcpu_ms"),
                        rs.getBigDecimal("cold_start")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load execution_metrics for revision=" + revision, e);
        }
    }
}
