package com.company.aneon.f01.repository;

import java.util.UUID;

import com.company.aneon.f01.constant.StoreStatus;
import com.company.aneon.f01.entity.CuaHangSap;
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
 * Truy van cua hang/sap co scope qua premise (F01UC02.1).
 */
@Repository
public class CuaHangSapQueryDao {

    private final EntityManager em;

    public CuaHangSapQueryDao(EntityManager em) {
        this.em = em;
    }

    public record StoreFilter(UUID premiseId, StoreStatus status, String q, AccessScope scope) {
    }

    public Page<CuaHangSap> search(StoreFilter filter, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<CuaHangSap> cq = cb.createQuery(CuaHangSap.class);
        Root<CuaHangSap> root = cq.from(CuaHangSap.class);
        cq.where(buildPredicates(cb, root, filter));
        cq.orderBy(cb.asc(root.get("code")), cb.asc(root.get("id")));
        TypedQuery<CuaHangSap> query = em.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<CuaHangSap> countRoot = countQuery.from(CuaHangSap.class);
        countQuery.select(cb.count(countRoot)).where(buildPredicates(cb, countRoot, filter));
        Long total = em.createQuery(countQuery).getSingleResult();
        return new PageImpl<>(query.getResultList(), pageable, total == null ? 0 : total);
    }

    private Predicate[] buildPredicates(CriteriaBuilder cb, Root<CuaHangSap> root, StoreFilter filter) {
        var predicates = new java.util.ArrayList<Predicate>();
        Join<CuaHangSap, ?> premise = root.join("premise", jakarta.persistence.criteria.JoinType.LEFT);
        if (filter.premiseId() != null) {
            predicates.add(cb.equal(premise.get("id"), filter.premiseId()));
        }
        if (filter.status() != null) {
            predicates.add(cb.equal(root.get("status"), filter.status()));
        }
        if (filter.q() != null && !filter.q().isBlank()) {
            String like = "%" + filter.q().trim().toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("code")), like),
                    cb.like(cb.lower(root.get("name")), like)));
        }
        predicates.add(cb.isNull(root.get("archivedAt")));
        if (!filter.scope().allZones()) {
            if (filter.scope().zoneIds().isEmpty()) {
                predicates.add(cb.disjunction());
            } else {
                predicates.add(premise.get("zone").get("id").in(filter.scope().zoneIds()));
            }
        }
        return predicates.toArray(Predicate[]::new);
    }
}
