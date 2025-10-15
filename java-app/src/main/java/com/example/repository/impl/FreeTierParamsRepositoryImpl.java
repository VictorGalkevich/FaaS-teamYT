package com.example.repository.impl;

import com.example.domain.FreeTierParams;
import com.example.repository.interfaces.FreeTierParamsRepository;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class FreeTierParamsRepositoryImpl implements FreeTierParamsRepository {

    private static final String ALL = """
            SELECT calls, exec_ms, mem_mb_ms, cpu_mcpu_ms
            FROM free_tier
            ORDER BY id
            """;
    private final DataSource dataSource;

    public FreeTierParamsRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public FreeTierParams getFreeTierParams() {
        final String sql = """
                SELECT calls, exec_ms, mem_mb_ms, cpu_mcpu_ms
                FROM free_tier
                ORDER BY id
                LIMIT 1
                """;
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                throw new IllegalStateException("free_tier is empty");
            }
            return new FreeTierParams(
                    rs.getBigDecimal("calls"),
                    rs.getBigDecimal("exec_ms"),
                    rs.getBigDecimal("mem_mb_ms"),
                    rs.getBigDecimal("cpu_mcpu_ms")
            );
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load free_tier", e);
        }
    }

    @Override
    public void save(FreeTierParams freeTierParams) {
        final String sql = """
                INSERT INTO free_tier(calls, exec_ms, mem_mb_ms, cpu_mcpu_ms)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setBigDecimal(1, freeTierParams.freeTierCalls());
            ps.setBigDecimal(2, freeTierParams.freeTierExecutionMs());
            ps.setBigDecimal(3, freeTierParams.freeTierMbMs());
            ps.setBigDecimal(4, freeTierParams.freeTierMcpuMs());

            int rows = ps.executeUpdate();
            if (rows != 1) {
                throw new RuntimeException("Expected to insert 1 row into rate_plans, but inserted: " + rows);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save free_tier", e);
        }
    }

    @Override
    public List<FreeTierParams> getAllRatePlans() {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(ALL);
             ResultSet rs = ps.executeQuery()) {

            var list = new ArrayList<FreeTierParams>();

            while (rs.next()) {
                list.add(
                        new FreeTierParams(
                                rs.getBigDecimal("calls"),
                                rs.getBigDecimal("exec_ms"),
                                rs.getBigDecimal("mem_mb_ms"),
                                rs.getBigDecimal("cpu_mcpu_ms")
                        )
                );
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load rate_plans", e);
        }
    }
}
