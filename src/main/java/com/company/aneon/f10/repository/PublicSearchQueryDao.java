package com.company.aneon.f10.repository;

import java.time.Instant;
import java.util.List;

import com.company.aneon.f10.dto.PublicDtos;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

/**
 * Phan trang hop nhat EVENT/PROMOTION cho public search (F10UC03).
 * Mot lan LIMIT/OFFSET tren UNION, sort on dinh (start_at, id), khong
 * phan trang rieng roi ghep sai (rule f10).
 */
@Repository
public class PublicSearchQueryDao {

    @PersistenceContext
    private EntityManager em;

    public record Filters(String q, String category, String availability, String type) { }

    private static final String UNION_SQL = """
            (select e.id, e.name as title, 'EVENT' as category, e.type,
                    e.start_at, e.end_at, v.text_description as location, null as terms
             from su_kien e
             left join vi_tri v on v.id = e.location_id
             where e.public_visible = true and e.archived_at is null
               and e.status in ('APPROVED', 'ONGOING') and e.end_at >= :now
               and e.start_at is not null
               and (:q is null or e.name like :q or e.type like :q or exists (
                   select 1 from su_kien_cua_hang link join cua_hang s on s.id = link.store_id
                   where link.event_id = e.id and s.public_visible = true and s.archived_at is null and s.name like :q))
               and (:category is null or :category = 'EVENT'))
            union all
            (select p.id, p.name as title, 'PROMOTION' as category, p.type,
                    p.start_at, p.end_at, null as location, p.terms
             from chuong_trinh_khuyen_mai p
             where p.public_visible = true and p.archived_at is null
               and p.status in ('SCHEDULED', 'ACTIVE') and p.end_at >= :now
               and p.start_at is not null
               and (:q is null or p.name like :q or p.type like :q or exists (
                   select 1 from khuyen_mai_cua_hang link join cua_hang s on s.id = link.store_id
                   where link.promotion_id = p.id and s.public_visible = true and s.archived_at is null and s.name like :q))
               and (:category is null or :category = 'PROMOTION'))
            """;
    private static final String FILTER_SQL = """
            where (:availability is null or (:availability = 'CURRENT' and start_at <= :now)
                    or (:availability = 'UPCOMING' and start_at > :now))
              and (:type is null or upper(type) = :type)
            """;

    public List<PublicDtos.PublicRow> searchPage(Filters filters, Instant now,
                                                 int limit, int offset) {
        var query = query("select * from (" + UNION_SQL + ") merged " + FILTER_SQL + "order by start_at, id", filters, now);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        var rows = (java.util.List<?>) query.getResultList();
        return rows.stream().map(row -> toRow((Object[]) row)).toList();
    }

    public long count(Filters filters, Instant now) {
        return ((Number) query("select count(*) from (" + UNION_SQL + ") merged " + FILTER_SQL, filters, now)
                .getSingleResult()).longValue();
    }

    private Query query(String sql, Filters filters, Instant now) {
        var query = em.createNativeQuery(sql);
        query.setParameter("now", java.sql.Timestamp.from(now));
        query.setParameter("q", filters.q() == null || filters.q().isBlank() ? null : "%" + filters.q().strip() + "%");
        query.setParameter("category", filters.category());
        query.setParameter("availability", filters.availability());
        query.setParameter("type", filters.type());
        return query;
    }

    private PublicDtos.PublicRow toRow(Object[] row) {
        java.sql.Timestamp start = (java.sql.Timestamp) row[4];
        java.sql.Timestamp end = (java.sql.Timestamp) row[5];
        return new PublicDtos.PublicRow(
                java.util.UUID.fromString((String) row[0]),
                (String) row[1],
                (String) row[2],
                (String) row[3],
                start == null ? null : start.toInstant(),
                end == null ? null : end.toInstant(),
                (String) row[6], (String) row[7]);
    }
}
