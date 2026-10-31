package com.company.aneon.f10.integration;

import java.util.Optional;
import java.util.UUID;

import com.company.aneon.shared.reference.TaiKhoanNguoiDungLookupRepository;

import org.springframework.stereotype.Component;

/**
 * Adapter that su doc tai_khoan_nguoi_dung (bang tham chieu trong baseline).
 */
@Component
public class PersonLookupAdapter implements PersonLookupPort {

    private final TaiKhoanNguoiDungLookupRepository accountRepository;

    public PersonLookupAdapter(TaiKhoanNguoiDungLookupRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public boolean accountExists(UUID accountId) {
        return accountId != null && accountRepository.existsById(accountId);
    }

    @Override
    public Optional<UUID> customerLinkOf(UUID accountId) {
        return accountRepository.findById(accountId).map(t -> t.getCustomerId());
    }
}
