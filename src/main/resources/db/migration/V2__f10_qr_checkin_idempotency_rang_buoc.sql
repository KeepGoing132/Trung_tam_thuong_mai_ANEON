-- =====================================================================
-- Rang buoc bo sung cho bat bien F10 (docs/decisions/0002):
--  - mot QR active moi su kien o muc DB
--  - moi khach mot luot check-in moi su kien
--  - idempotency ap dung khuyen mai / phat voucher
--  - phong thu them cho trung vi tri hoat dong (khong thay kiem tra
--    giao khoang trong service)
-- =====================================================================

-- MySQL khong co partial index: dung cot sinh (NULL khong biem unique)
ALTER TABLE ma_qr_tham_gia
  ADD COLUMN active_guard TINYINT GENERATED ALWAYS AS (IF(active = 1, 1, NULL)) VIRTUAL;
ALTER TABLE ma_qr_tham_gia
  ADD UNIQUE KEY uq_ma_qr_active_per_event (event_id, active_guard);

ALTER TABLE luot_tham_gia_su_kien
  ADD UNIQUE KEY uq_luot_tham_gia_event_customer (event_id, customer_id);

ALTER TABLE lan_ap_dung_khuyen_mai
  ADD UNIQUE KEY uq_lan_ap_dung_idempotency (idempotency_key);

ALTER TABLE phieu_uu_dai_khach_hang
  ADD UNIQUE KEY uq_phieu_uu_dai_kh_issue_ref (voucher_id, issue_reference);

ALTER TABLE hoat_dong_su_kien
  ADD COLUMN conflict_guard TINYINT GENERATED ALWAYS AS (IF(archived_at IS NULL, 1, NULL)) VIRTUAL;
ALTER TABLE hoat_dong_su_kien
  ADD UNIQUE KEY uq_hoat_dong_loc_time (location_id, start_at, end_at, conflict_guard);
