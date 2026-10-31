package com.company.aneon.f01.integration;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * STUB CO NHAN - chi profile local/test (rule 06: adapter gia phai co nhan).
 * Diem can luu y: stub tra isAvailable()=true va danh sach hop dong rong,
 * tuc "khong co hop dong nao" chu KHONG phai "khong xac minh duoc". Khi
 * tich hop F03 that, thay bean nay boi adapter goi dich vu F03.
 */
@Component
@Profile({"local", "test"})
public class LocalContractStubAdapter implements ContractQueryPort {

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public Optional<ContractSummary> findContract(UUID contractId) {
        return Optional.empty();
    }

    @Override
    public List<ContractSummary> findActiveContractsForPremise(UUID premiseId) {
        return List.of();
    }

    @Override
    public List<ContractSummary> findActiveContractsForPremiseExcluding(UUID premiseId, UUID excludingContractId) {
        return List.of();
    }
}
