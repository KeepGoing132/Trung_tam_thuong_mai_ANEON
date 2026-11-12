package com.company.aneon.f10.integration;

import com.company.aneon.shared.reference.HoaDonMuaHangLookupRepository;
import com.company.aneon.shared.reference.KhachMuaSamLookupRepository;

import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that su doc khach_mua_sam / hoa_don_mua_hang (bang tham chieu).
 * Khi tich hop F11 that, thay boi adapter goi dich vu F11.
 */
@Component
public class CustomerInvoiceLookupAdapter implements CustomerLookupPort, InvoiceLookupPort {

    private final KhachMuaSamLookupRepository customerRepository;
    private final HoaDonMuaHangLookupRepository invoiceRepository;
    private final JdbcClient jdbc;

    public CustomerInvoiceLookupAdapter(KhachMuaSamLookupRepository customerRepository,
                                        HoaDonMuaHangLookupRepository invoiceRepository, JdbcClient jdbc) {
        this.customerRepository = customerRepository;
        this.invoiceRepository = invoiceRepository;
        this.jdbc = jdbc;
    }

    @Override
    public boolean customerExists(UUID customerId) {
        return customerId != null && customerRepository.existsById(customerId);
    }

    @Override
    public List<String> activeMembershipTiers(UUID customerId) {
        return jdbc.sql("""
                select distinct tier from tai_khoan_thanh_vien
                where customer_id = :customer and status = 'ACTIVE' and archived_at is null and tier is not null
                """).param("customer", customerId.toString()).query(String.class).list();
    }

    @Override
    public Optional<InvoiceRef> findInvoice(UUID invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .map(i -> new InvoiceRef(i.getId(), i.getCustomerId(), i.getStoreId(),
                        i.getTotalAmount(), i.isVerified()));
    }
}
