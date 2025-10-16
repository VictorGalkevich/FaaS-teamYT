package com.example.repository.impl;

import com.example.domain.ExecutionMetrics;
import com.example.repository.interfaces.MetricsRepository;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;

@Repository
public class MetricsRepositoryImpl implements MetricsRepository {

    private final DataSource dataSource;

    public MetricsRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public ExecutionMetrics getExecutionMetricsByRevisionX(String revision) {
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

    public ExecutionMetrics getExecutionMetricsByName(Timestamp fromTs, Timestamp toTs, String name) throws SQLException {
        String sql = """
                    SELECT
                      name,
                      sum(request_count_delta) AS total_requests,
                      sum(total_time_ms_delta) AS total_exec_time_ms,
                      sum(cold_start_ms_delta) AS total_cold_start_ms,
                      avg(cpu_milli)           AS avg_cpu_milli,
                      avg(memory_mib)          AS avg_memory_mib
                    FROM execution_metrics
                    WHERE timestamp >= ? AND timestamp < ? AND name = ?
                    GROUP BY name
                    ORDER BY name
                """;

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setTimestamp(1, fromTs);
            ps.setTimestamp(2, toTs);
            ps.setString(3, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    var calls = rs.getBigDecimal("total_requests");
                    var exec = rs.getBigDecimal("total_exec_time_ms");
                    var mem = rs.getBigDecimal("avg_memory_mib");
                    var cpu = rs.getBigDecimal("avg_cpu_milli");
                    var cold_start = rs.getBigDecimal("total_cold_start_ms");

                    return new ExecutionMetrics(
                            calls,
                            exec.subtract(cold_start),
                            mem.multiply(exec),
                            cpu.multiply(exec),
                            cold_start
                    );
                } else {
                    throw new SQLException("Нет данных для " + name);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }
}
