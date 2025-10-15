package com.example.repository.impl;

import com.example.domain.RatePlans;
import com.example.repository.interfaces.RatePlansRepository;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class RatePlansRepositoryImpl implements RatePlansRepository {

    private final DataSource dataSource;

    public RatePlansRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public RatePlans getRatePlans() {
        final String sql = """
            SELECT per_call, exec_per_ms, mem_per_mb_ms, cpu_per_mcpu_ms, cold_start_fee
            FROM rate_plans
            ORDER BY id
            LIMIT 1
            """;
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                throw new IllegalStateException("rate_plans is empty");
            }
            return new RatePlans(
                    rs.getBigDecimal("per_call"),
                    rs.getBigDecimal("exec_per_ms"),
                    rs.getBigDecimal("mem_per_mb_ms"),
                    rs.getBigDecimal("cpu_per_mcpu_ms"),
                    rs.getBigDecimal("cold_start_fee")
            );
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load rate_plans", e);
        }
    }
}
