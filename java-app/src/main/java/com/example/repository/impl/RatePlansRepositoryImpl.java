package com.example.repository.impl;

import com.example.domain.RatePlans;
import com.example.repository.interfaces.RatePlansRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class RatePlansRepositoryImpl implements RatePlansRepository {

    private static final String CURRENT = """
                SELECT per_call, exec_per_ms, mem_per_mb_ms, cpu_per_mcpu_ms, cold_start_fee
                FROM rate_plans
                ORDER BY id desc
                LIMIT 1
                """;

    private static final String ALL = """
            SELECT per_call, exec_per_ms, mem_per_mb_ms, cpu_per_mcpu_ms, cold_start_fee
            FROM rate_plans
            ORDER BY id
            """;

    private final DataSource dataSource;

    public RatePlansRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public RatePlans getRatePlans() {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(CURRENT);
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

    @Override
    public void saveRatePlans(RatePlans ratePlans) {
        final String sql = """
            INSERT INTO rate_plans
              (per_call, exec_per_ms, mem_per_mb_ms, cpu_per_mcpu_ms, cold_start_fee)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setBigDecimal(1, ratePlans.pricePerCall());
            ps.setBigDecimal(2, ratePlans.pricePerMsOfExec());
            ps.setBigDecimal(3, ratePlans.pricePerMbMsOfMem());
            ps.setBigDecimal(4, ratePlans.pricePerMcpuMsOfCpu());
            ps.setBigDecimal(5, ratePlans.coldStartFee());

            int updated = ps.executeUpdate();
            if (updated != 1) {
                throw new RuntimeException("Expected to insert 1 row into rate_plans, but inserted: " + updated);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert into rate_plans", e);
        }
    }

    @Override
    public List<RatePlans> getAllRatePlans() {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(ALL);
             ResultSet rs = ps.executeQuery()) {

            var list = new ArrayList<RatePlans>();

            while (rs.next()) {
                list.add(
                        new RatePlans(
                                rs.getBigDecimal("per_call"),
                                rs.getBigDecimal("exec_per_ms"),
                                rs.getBigDecimal("mem_per_mb_ms"),
                                rs.getBigDecimal("cpu_per_mcpu_ms"),
                                rs.getBigDecimal("cold_start_fee")
                        )
                );
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load rate_plans", e);
        }
    }
}
