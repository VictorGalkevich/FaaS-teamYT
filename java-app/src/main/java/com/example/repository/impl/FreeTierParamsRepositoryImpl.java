package com.example.repository.impl;

import com.example.domain.FreeTierParams;
import com.example.repository.interfaces.FreeTierParamsRepository;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class FreeTierParamsRepositoryImpl implements FreeTierParamsRepository {

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
}
