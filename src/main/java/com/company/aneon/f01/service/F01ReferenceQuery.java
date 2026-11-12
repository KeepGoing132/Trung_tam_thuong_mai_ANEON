package com.company.aneon.f01.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.company.aneon.f01.integration.DigitalMapPort;
import com.company.aneon.shared.error.ApiException;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class F01ReferenceQuery {
    public record Tenant(UUID id, String code, String name) { }

    private final MatBangService premises;
    private final CuaHangSapService stores;
    private final DigitalMapPort maps;
    private final JdbcClient jdbc;
    private final Clock clock;

    public F01ReferenceQuery(MatBangService premises, CuaHangSapService stores, DigitalMapPort maps,
                             JdbcClient jdbc, Clock clock) {
        this.premises = premises;
        this.stores = stores;
        this.maps = maps;
        this.jdbc = jdbc;
        this.clock = clock;
    }

    public DigitalMapPort.FloorMap premiseMap(UUID id) {
        var premise = premises.detail(id);
        return maps.findForZone(premise.zoneId())
                .orElseThrow(() -> ApiException.notFound("So do tang da phat hanh", premise.zoneId()));
    }

    public List<Tenant> storeTenants(UUID id) {
        var store = stores.detail(id);
        if (store.premiseId() == null) return List.of();
        return jdbc.sql("""
                select distinct t.id, t.code, t.name
                from khach_thue t join lien_ket_khai_thac l on l.tenant_id = t.id
                where l.store_id = :store and l.archived_at is null and t.archived_at is null
                  and l.status = 'ACTIVE' and l.unlinked_at is null
                  and (l.range_start is null or l.range_start <= :today)
                  and (l.range_end is null or l.range_end >= :today)
                order by t.name, t.id
                """).param("store", id.toString()).param("today", LocalDate.now(clock))
                .query((row, index) -> new Tenant(UUID.fromString(row.getString("id")),
                        row.getString("code"), row.getString("name"))).list();
    }
}
