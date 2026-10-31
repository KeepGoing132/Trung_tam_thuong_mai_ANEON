package com.company.aneon.f01.integration;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port truy van hop dong F03. F01 khong doc repository F03 truc tiep (rule 06).
 * Trang thai tra ve la chuoi ma F03 so huu (DRAFT/SIGNED/ACTIVE/EXPIRED/
 * TERMINATED/CANCELLED theo mo hinh); F01 map thanh trang thai mat bang.
 */
public interface ContractQueryPort {

    record ContractSummary(UUID id, String status, UUID premiseId, UUID storeId,
                           Instant startAt, Instant endAt) {
    }

    /** false khi khong the xac minh du lieu hop dong -> 503 DEPENDENCY_UNAVAILABLE. */
    boolean isAvailable();

    Optional<ContractSummary> findContract(UUID contractId);

    /** Cac hop dang hieu luc cua mat bang (dang occupancy, khong tinh archived). */
    List<ContractSummary> findActiveContractsForPremise(UUID premiseId);

    /** Cac hop dang hieu luc, loai tru mot hop dong cu the (khi dong bo). */
    List<ContractSummary> findActiveContractsForPremiseExcluding(UUID premiseId, UUID excludingContractId);
}
