package com.company.aneon.f10.integration;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Port hoa don F11 de xac minh du lieu that khi ap dung khuyen mai/redeem;
 * khong tin so tien client khai (rule f10).
 */
public interface InvoiceLookupPort {

    record InvoiceRef(UUID id, UUID customerId, UUID storeId, BigDecimal totalAmount, boolean verified) {
    }

    Optional<InvoiceRef> findInvoice(UUID invoiceId);
}
