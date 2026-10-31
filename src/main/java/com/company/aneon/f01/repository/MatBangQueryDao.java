package com.company.aneon.f01.repository;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.company.aneon.f01.constant.PremiseStatus;
import com.company.aneon.f01.entity.MatBang;
import com.company.aneon.shared.security.AccessScope;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Truy van mat bang co bo loc dung chung cho list/utilization (F01UC01.1,
 * F01UC05.1-05.4) va tong hop summary tren toan bo tap da loc (khong chi
 * trang hien tai). Scope ap trong SQL truoc phan trang.
 */
@Repository
public class MatBangQueryDao {

    private final EntityManager em;

    public MatBangQueryDao(EntityManager em) {
        this.em = em;
    }

    public record PremiseFilter(String q, UUID zoneId, UUID floorId, PremiseStatus status, AccessScope scope) {
    }

    public Page<MatBang> search(PremiseFilter filter, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MatBang> cq = cb.createQuery(MatBang.class);
        Root<MatBang> root = cq.from(MatBang.class);
        Join<?, ?> zone = root.join("zone");
        cq.where(buildPredicates(cb, root, zone, filter));
        cq.orderBy(cb.asc(root.get("code")), cb.asc(root.get("id")));
        TypedQuery<MatBang> query = em.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<MatBang> countRoot = countQuery.from(MatBang.class);
        Join<?, ?> countZone = countRoot.join("zone");
        countQuery.select(cb.count(countRoot))
                .where(buildPredicates(cb, countRoot, countZone, filter));
        Long total = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(query.getResultList(), pageable, total == null ? 0 : total);
    }

    /** Dem theo trang thai tren toan bo tap da loc (F01UC05.1). */
    public Map<PremiseStatus, Long> countByStatus(PremiseFilter filter) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<MatBang> root = cq.from(MatBang.class);
        Join<?, ?> zone = root.join("zone");
        cq.multiselect(root.get("status"), cb.count(root))
                .where(buildPredicates(cb, root, zone, filter))
                .groupBy(root.get("status"));
        return em.createQuery(cq).getResultList().stream()
                .collect(Collectors.toUnmodifiableMap(row -> (PremiseStatus) row[0], row -> (Long) row[1]));
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<MatBang> root, Join<?, ?> zone,
                                        PremiseFilter filter) {
        var predicates = new java.util.ArrayList<Predicate>();
        if (filter.q() != null && !filter.q().isBlank()) {
            String like = "%" + filter.q().trim().toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("code")), like),
                    cb.like(cb.lower(root.get("name")), like)));
        }
        if (filter.zoneId() != null) {
            predicates.add(cb.equal(zone.get("id"), filter.zoneId()));
        }
        if (filter.floorId() != null) {
            predicates.add(cb.equal(zone.get("floor").get("id"), filter.floorId()));
        }
        if (filter.status() != null) {
            predicates.add(cb.equal(root.get("status"), filter.status()));
        }
        predicates.add(cb.isNull(root.get("archivedAt")));
        predicates.add(scopePredicate(cb, zone, filter.scope()));
        return predicates.toArray(Predicate[]::new);
    }

    static Predicate scopePredicate(CriteriaBuilder cb, Join<?, ?> zone, AccessScope scope) {
        if (scope.allZones()) {
            return cb.conjunction();
        }
        if (scope.zoneIds().isEmpty()) {
            return cb.disjunction();
        }
        return zone.get("id").in(scope.zoneIds());
    }
}
