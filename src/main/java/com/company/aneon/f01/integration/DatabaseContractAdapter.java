package com.company.aneon.f01.integration;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class DatabaseContractAdapter implements ContractQueryPort {
    private final JdbcClient jdbc;
    private final Clock clock;

    public DatabaseContractAdapter(JdbcClient jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Override
    public boolean isAvailable() {
        try {
            jdbc.sql("select 1 from hop_dong_thue limit 1").query(Integer.class).optional();
            return true;
        } catch (DataAccessException unavailable) {
            return false;
        }
    }

    @Override
    public Optional<ContractSummary> findContract(UUID contractId) {
        return jdbc.sql("""
                select id, status, premise_id, store_id, range_start, range_end
                from hop_dong_thue where id = :id and archived_at is null
                """).param("id", contractId.toString()).query(mapper()).optional();
    }

    @Override
    public List<ContractSummary> findActiveContractsForPremise(UUID premiseId) {
        return findActiveContractsForPremiseExcluding(premiseId, null);
    }

    @Override
    public List<ContractSummary> findActiveContractsForPremiseExcluding(UUID premiseId, UUID excludingContractId) {
        return jdbc.sql("""
                select id, status, premise_id, store_id, range_start, range_end
                from hop_dong_thue
                where premise_id = :premise and archived_at is null and status = 'ACTIVE'
                  and range_start <= :today and (range_end is null or range_end >= :today)
                  and (:excluded is null or id <> :excluded)
                """).param("premise", premiseId.toString()).param("today", LocalDate.now(clock))
                .param("excluded", excludingContractId == null ? null : excludingContractId.toString())
                .query(mapper()).list();
    }

    private RowMapper<ContractSummary> mapper() {
        return (row, index) -> {
            String storeId = row.getString("store_id");
            LocalDate end = row.getObject("range_end", LocalDate.class);
            return new ContractSummary(UUID.fromString(row.getString("id")), row.getString("status"),
                    UUID.fromString(row.getString("premise_id")), storeId == null ? null : UUID.fromString(storeId),
                    row.getObject("range_start", LocalDate.class).atStartOfDay(clock.getZone()).toInstant(),
                    end == null ? null : end.plusDays(1).atStartOfDay(clock.getZone()).toInstant());
        };
    }
}
