-- Bảng tham chiếu từ thiết kế F02/F03/F11/F18 được F01 và F10 sử dụng.

CREATE TABLE khach_thue (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  code VARCHAR(50) NOT NULL COMMENT 'Ma khach thue',
  name VARCHAR(200) NOT NULL COMMENT 'Ten khach thue',
  type VARCHAR(40) COMMENT 'Loai khach thue (TenantType)',
  phone VARCHAR(30) COMMENT 'Dien thoai',
  email VARCHAR(200) COMMENT 'Email',
  address VARCHAR(500) COMMENT 'Dia chi',
  tax_code VARCHAR(50) COMMENT 'Ma so thue',
  representative_name VARCHAR(200) COMMENT 'Nguoi dai dien',
  status VARCHAR(40) NOT NULL COMMENT 'Trang thai (TenantStatus)',
  notes VARCHAR(1000) COMMENT 'Ghi chu',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id),
  UNIQUE KEY uq_khach_thue_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Khach thue (ca nhan / to chuc).';

CREATE TABLE hop_dong_thue (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  number VARCHAR(50) NOT NULL COMMENT 'So hop dong',
  tenant_id CHAR(36) NOT NULL COMMENT 'Khach thue',
  premise_id CHAR(36) NOT NULL COMMENT 'Mat bang',
  store_id CHAR(36) COMMENT 'Cua hang',
  leased_area DECIMAL(14,2) COMMENT 'Dien tich thue (m2)',
  range_start DATE NOT NULL COMMENT 'Ngay bat dau hieu luc',
  range_end DATE COMMENT 'Ngay ket thuc hieu luc',
  payment_cycle VARCHAR(40) COMMENT 'Chu ky thanh toan (PaymentCycle)',
  deposit_amount DECIMAL(18,2) COMMENT 'Tien dat coc',
  status VARCHAR(40) NOT NULL COMMENT 'Trang thai hop dong (ContractStatus)',
  signed_at DATETIME(6) COMMENT 'Thoi diem ky',
  terminated_at DATETIME(6) COMMENT 'Thoi diem ket thuc',
  notes VARCHAR(1000) COMMENT 'Ghi chu',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id),
  UNIQUE KEY uq_hop_dong_number (number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Hop dong thue mat bang / cua hang.';

CREATE TABLE lien_ket_khai_thac (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  tenant_id CHAR(36) NOT NULL COMMENT 'Khach thue',
  premise_id CHAR(36) COMMENT 'Mat bang',
  store_id CHAR(36) COMMENT 'Cua hang',
  contract_id CHAR(36) COMMENT 'Hop dong',
  range_start DATE COMMENT 'Ngay bat dau',
  range_end DATE COMMENT 'Ngay ket thuc',
  status VARCHAR(40) NOT NULL COMMENT 'Trang thai (OccupancyStatus)',
  linked_at DATETIME(6) COMMENT 'Thoi diem lien ket',
  unlinked_at DATETIME(6) COMMENT 'Thoi diem huy lien ket',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Lien ket khach thue voi mat bang / cua hang.';

CREATE TABLE so_do_tang (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  floor_id CHAR(36) NOT NULL COMMENT 'Tang',
  name VARCHAR(200) NOT NULL COMMENT 'Ten so do',
  background_id CHAR(36) COMMENT 'Anh nen',
  bounds_min_x DECIMAL(14,4) COMMENT 'Bien nho nhat X',
  bounds_min_y DECIMAL(14,4) COMMENT 'Bien nho nhat Y',
  bounds_max_x DECIMAL(14,4) COMMENT 'Bien lon nhat X',
  bounds_max_y DECIMAL(14,4) COMMENT 'Bien lon nhat Y',
  scale DECIMAL(14,6) COMMENT 'Ty le',
  published BOOLEAN NOT NULL DEFAULT 0 COMMENT 'Da phat hanh',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='So do tang (ban do).';

CREATE TABLE chinh_sach_thanh_vien (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  effective_start DATE COMMENT 'Hieu luc tu ngay',
  effective_end DATE COMMENT 'Hieu luc den ngay',
  silver_threshold DECIMAL(18,2) COMMENT 'Nguong hang Bac',
  gold_threshold DECIMAL(18,2) COMMENT 'Nguong hang Vang',
  diamond_threshold DECIMAL(18,2) COMMENT 'Nguong hang Kim cuong',
  spend_per_point DECIMAL(18,2) COMMENT 'Chi tieu moi diem',
  points_per_conversion INT COMMENT 'So diem moi lan quy doi',
  gold_multiplier DECIMAL(9,4) COMMENT 'He so hang Vang',
  diamond_multiplier DECIMAL(9,4) COMMENT 'He so hang Kim cuong',
  point_value DECIMAL(18,2) COMMENT 'Gia tri 1 diem',
  point_rounding_mode VARCHAR(30) COMMENT 'Che do lam tron (RoundingMode)',
  tier_period VARCHAR(40) COMMENT 'Ky xet hang (PeriodType)',
  active BOOLEAN NOT NULL DEFAULT 1 COMMENT 'Dang hieu luc',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Chinh sach thanh vien.';

CREATE TABLE tai_khoan_thanh_vien (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  customer_id CHAR(36) NOT NULL COMMENT 'Khach mua sam',
  card_number VARCHAR(50) NOT NULL COMMENT 'So the',
  tier VARCHAR(40) COMMENT 'Hang thanh vien (MembershipTier)',
  points_balance BIGINT NOT NULL DEFAULT 0 COMMENT 'So diem hien co',
  qualified_spend DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT 'Chi tieu tich luy',
  qualification_start DATE COMMENT 'Ky xet tu ngay',
  qualification_end DATE COMMENT 'Ky xet den ngay',
  status VARCHAR(40) COMMENT 'Trang thai (MembershipStatus)',
  policy_id CHAR(36) NOT NULL COMMENT 'Chinh sach ap dung',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id),
  UNIQUE KEY uq_tai_khoan_thanh_vien_card (card_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tai khoan thanh vien.';

ALTER TABLE hop_dong_thue ADD CONSTRAINT fk_hop_dong_thue_tenant_id FOREIGN KEY (tenant_id) REFERENCES khach_thue(id) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE hop_dong_thue ADD CONSTRAINT fk_hop_dong_thue_premise_id FOREIGN KEY (premise_id) REFERENCES mat_bang(id) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE hop_dong_thue ADD CONSTRAINT fk_hop_dong_thue_store_id FOREIGN KEY (store_id) REFERENCES cua_hang(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE lien_ket_khai_thac ADD CONSTRAINT fk_lien_ket_khai_thac_tenant_id FOREIGN KEY (tenant_id) REFERENCES khach_thue(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE lien_ket_khai_thac ADD CONSTRAINT fk_lien_ket_khai_thac_premise_id FOREIGN KEY (premise_id) REFERENCES mat_bang(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE lien_ket_khai_thac ADD CONSTRAINT fk_lien_ket_khai_thac_store_id FOREIGN KEY (store_id) REFERENCES cua_hang(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE lien_ket_khai_thac ADD CONSTRAINT fk_lien_ket_khai_thac_contract_id FOREIGN KEY (contract_id) REFERENCES hop_dong_thue(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE so_do_tang ADD CONSTRAINT fk_so_do_tang_floor_id FOREIGN KEY (floor_id) REFERENCES tang(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE so_do_tang ADD CONSTRAINT fk_so_do_tang_background_id FOREIGN KEY (background_id) REFERENCES tep_dinh_kem(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE tai_khoan_thanh_vien ADD CONSTRAINT fk_tai_khoan_thanh_vien_customer_id FOREIGN KEY (customer_id) REFERENCES khach_mua_sam(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE tai_khoan_thanh_vien ADD CONSTRAINT fk_tai_khoan_thanh_vien_policy_id FOREIGN KEY (policy_id) REFERENCES chinh_sach_thanh_vien(id) ON DELETE RESTRICT ON UPDATE RESTRICT;
