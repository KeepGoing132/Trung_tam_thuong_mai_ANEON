package com.company.aneon.f10.repository;

import java.time.Instant;
import java.util.List;

import com.company.aneon.f10.dto.PublicDtos;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Phan trang hop nhat EVENT/PROMOTION cho public search (F10UC03).
 * Mot lan LIMIT/OFFSET tren UNION, sort on dinh (start_at, id), khong
 * phan trang rieng roi ghep sai (rule f10).
 */
@Repository
public class PublicSearchQueryDao {

    @PersistenceContext
    private EntityManager em;

    private static final String UNION_SQL = """
            (select e.id, e.name as title, 'EVENT' as category, e.type,
                    e.start_at, e.end_at, v.text_description as location
             from su_kien e
             left join vi_tri v on v.id = e.location_id
             where e.public_visible = true and e.archived_at is null
               and e.status in ('APPROVED', 'ONGOING') and e.end_at >= :now
               and (:q is null or lower(e.name) like lower(concat('%', :q, '%')))
               and (:category is null or :category = 'EVENT'))
            union all
            (select p.id, p.name as title, 'PROMOTION' as category, p.type,
                    p.start_at, p.end_at, null as location
             from chuong_trinh_khuyen_mai p
             where p.public_visible = true and p.archived_at is null
               and p.status in ('SCHEDULED', 'ACTIVE') and p.end_at >= :now
               and (:q is null or lower(p.name) like lower(concat('%', :q, '%')))
               and (:category is null or :category = 'PROMOTION'))
            order by start_at asc, id asc
            """;

    public List<PublicDtos.PublicRow> searchPage(String q, String category, Instant now,
                                                 int limit, int offset) {
        var query = em.createNativeQuery(UNION_SQL);
        query.setParameter("now", java.sql.Timestamp.from(now));
        query.setParameter("q", q == null || q.isBlank() ? null : q.trim());
        query.setParameter("category", category);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        var rows = (java.util.List<?>) query.getResultList();
        return rows.stream().map(row -> toRow((Object[]) row)).toList();
    }

    public long count(String q, String category, Instant now) {
        var query = em.createNativeQuery("select count(*) from (" + UNION_SQL + ") merged");
        query.setParameter("now", java.sql.Timestamp.from(now));
        query.setParameter("q", q == null || q.isBlank() ? null : q.trim());
        query.setParameter("category", category);
        Object result = query.getSingleResult();
        return ((Number) result).longValue();
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
                (String) row[6]);
    }
}
