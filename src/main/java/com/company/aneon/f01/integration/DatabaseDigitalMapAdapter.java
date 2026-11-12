package com.company.aneon.f01.integration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class DatabaseDigitalMapAdapter implements DigitalMapPort {
    private final JdbcClient jdbc;

    public DatabaseDigitalMapAdapter(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean isAvailable() {
        try {
            jdbc.sql("select 1 from so_do_tang limit 1").query(Integer.class).optional();
            return true;
        } catch (DataAccessException unavailable) {
            return false;
        }
    }

    @Override
    public boolean isLocationMappable(UUID locationId) {
        var zone = jdbc.sql("select zone_id from vi_tri where id = :id and archived_at is null")
                .param("id", locationId.toString()).query(String.class).optional();
        return zone.flatMap(id -> findForZone(UUID.fromString(id)))
                .stream().flatMap(map -> map.points().stream()).anyMatch(point -> point.id().equals(locationId));
    }

    @Override
    public Optional<FloorMap> findForZone(UUID zoneId) {
        return jdbc.sql("""
                select m.id, m.name, m.bounds_min_x, m.bounds_min_y, m.bounds_max_x, m.bounds_max_y
                from so_do_tang m join khu_vuc z on z.floor_id = m.floor_id
                where z.id = :zone and z.archived_at is null and m.published = true and m.archived_at is null
                  and m.bounds_min_x < m.bounds_max_x and m.bounds_min_y < m.bounds_max_y
                order by m.created_at desc, m.id desc limit 1
                """).param("zone", zoneId.toString()).query((row, index) -> new FloorMap(
                UUID.fromString(row.getString("id")), row.getString("name"), row.getBigDecimal("bounds_min_x"),
                row.getBigDecimal("bounds_min_y"), row.getBigDecimal("bounds_max_x"),
                row.getBigDecimal("bounds_max_y"), List.of())).optional().map(map -> new FloorMap(
                map.id(), map.name(), map.minX(), map.minY(), map.maxX(), map.maxY(), points(zoneId, map)));
    }

    private List<MapPoint> points(UUID zoneId, FloorMap map) {
        return jdbc.sql("""
                select id, x, y, text_description from vi_tri
                where zone_id = :zone and archived_at is null
                  and x between :minX and :maxX and y between :minY and :maxY
                order by text_description, id
                """).param("zone", zoneId.toString()).param("minX", map.minX()).param("minY", map.minY())
                .param("maxX", map.maxX()).param("maxY", map.maxY())
                .query((row, index) -> new MapPoint(UUID.fromString(row.getString("id")),
                        row.getBigDecimal("x"), row.getBigDecimal("y"), row.getString("text_description"))).list();
    }
}
