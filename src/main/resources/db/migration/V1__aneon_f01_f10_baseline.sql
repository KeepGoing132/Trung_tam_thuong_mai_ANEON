-- =====================================================================
-- Baseline toi thieu F01 (8 bang) + F10 (15 bang) + bang dung chung duoc
-- tham chieu boi FK that. Sao chep nguyen van cau truc tu ../schema.sql
-- (MySQL 8.0, InnoDB, utf8mb4_unicode_ci, CHAR(36), DATETIME(6)).
-- Khong CREATE DATABASE/USE: ten database do cau hinh datasource quyet dinh.
-- FK tro ra ngoai baseline (tai_khoan_nguoi_dung.tenant_id -> khach_thue)
-- khong dua vao, ghi ro tai docs/decisions/0002-baseline-csdl-toi-thieu.md.
-- =====================================================================

-- [F16] vai_tro
CREATE TABLE vai_tro (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  code VARCHAR(50) NOT NULL COMMENT 'Ma vai tro (RoleCode)',
  name VARCHAR(200) NOT NULL COMMENT 'Ten vai tro',
  description VARCHAR(500) COMMENT 'Mo ta',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id),
  UNIQUE KEY uq_vai_tro_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Vai tro nguoi dung.';

-- [F11] khach_mua_sam
CREATE TABLE khach_mua_sam (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  full_name VARCHAR(200) NOT NULL COMMENT 'Ho ten',
  phone VARCHAR(30) COMMENT 'Dien thoai',
  email VARCHAR(200) COMMENT 'Email',
  gender VARCHAR(20) COMMENT 'Gioi tinh',
  date_of_birth DATE COMMENT 'Ngay sinh',
  license_plate VARCHAR(30) COMMENT 'Bien so xe',
  registered_at DATETIME(6) COMMENT 'Thoi diem dang ky',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id),
  UNIQUE KEY uq_khach_mua_sam_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Khach mua sam.';

-- [F16] tai_khoan_nguoi_dung
CREATE TABLE tai_khoan_nguoi_dung (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  username VARCHAR(100) NOT NULL COMMENT 'Ten dang nhap',
  full_name VARCHAR(200) COMMENT 'Ho ten',
  email VARCHAR(200) COMMENT 'Email',
  phone VARCHAR(30) COMMENT 'Dien thoai',
  password_hash VARCHAR(255) COMMENT 'Mat khau (hash)',
  role_id CHAR(36) NOT NULL COMMENT 'Vai tro',
  status VARCHAR(40) COMMENT 'Trang thai (AccountStatus)',
  auth_version BIGINT NOT NULL DEFAULT 0 COMMENT 'Phien ban xac thuc',
  tenant_id CHAR(36) COMMENT 'Khach thue lien ket',
  customer_id CHAR(36) COMMENT 'Khach mua sam lien ket',
  employee_code VARCHAR(50) COMMENT 'Ma nhan vien',
  department VARCHAR(200) COMMENT 'Phong ban',
  last_login_at DATETIME(6) COMMENT 'Dang nhap gan nhat',
  lock_reason VARCHAR(500) COMMENT 'Ly do khoa',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id),
  UNIQUE KEY uq_tai_khoan_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tai khoan nguoi dung (dung chung).';

-- [dung chung] tep_dinh_kem
CREATE TABLE tep_dinh_kem (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  target_type VARCHAR(40) COMMENT 'Loai doi tuong (ThamChieuDoiTuong)',
  target_id CHAR(36) COMMENT 'Id doi tuong',
  original_name VARCHAR(300) COMMENT 'Ten tep goc',
  media_type VARCHAR(100) COMMENT 'Media type',
  size_bytes BIGINT COMMENT 'Kich thuoc (byte)',
  storage_key VARCHAR(300) COMMENT 'Khoa luu tru',
  sha256 VARCHAR(64) COMMENT 'Ma bam SHA-256',
  uploaded_by CHAR(36) COMMENT 'Nguoi tai len',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id),
  KEY ix_tep_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tep dinh kem (dung chung).';

-- [F01] toa_nha
CREATE TABLE toa_nha (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  code VARCHAR(50) NOT NULL COMMENT 'Ma toa nha',
  name VARCHAR(200) NOT NULL COMMENT 'Ten toa nha',
  address VARCHAR(500) COMMENT 'Dia chi',
  active BOOLEAN NOT NULL DEFAULT 1 COMMENT 'Dang hoat dong',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id),
  UNIQUE KEY uq_toa_nha_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Toa nha thuoc TTTM ANEON.';

-- [F01] tang
CREATE TABLE tang (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  building_id CHAR(36) NOT NULL COMMENT 'Toa nha',
  code VARCHAR(50) NOT NULL COMMENT 'Ma tang',
  name VARCHAR(200) NOT NULL COMMENT 'Ten tang',
  level INT NOT NULL COMMENT 'Chi so tang (0 = tang tret)',
  active BOOLEAN NOT NULL DEFAULT 1 COMMENT 'Dang hoat dong',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id),
  UNIQUE KEY uq_tang_code (building_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Tang (lau) thuoc toa nha.';

-- [F01] khu_vuc
CREATE TABLE khu_vuc (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  floor_id CHAR(36) NOT NULL COMMENT 'Tang',
  code VARCHAR(50) NOT NULL COMMENT 'Ma khu vuc',
  name VARCHAR(200) NOT NULL COMMENT 'Ten khu vuc',
  description VARCHAR(500) COMMENT 'Mo ta',
  active BOOLEAN NOT NULL DEFAULT 1 COMMENT 'Dang hoat dong',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id),
  UNIQUE KEY uq_khu_vuc_code (floor_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Khu vuc tren tang.';

-- [F01] vi_tri
CREATE TABLE vi_tri (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  zone_id CHAR(36) NOT NULL COMMENT 'Khu vuc',
  x DECIMAL(14,4) COMMENT 'Toa do X',
  y DECIMAL(14,4) COMMENT 'Toa do Y',
  text_description VARCHAR(500) COMMENT 'Mo ta vi tri bang loi',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Toa do vi tri trong khu vuc (dung chung cho F09/F10/F17/F18).';

-- [F01] nganh_hang
CREATE TABLE nganh_hang (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  code VARCHAR(50) NOT NULL COMMENT 'Ma nganh hang',
  name VARCHAR(200) NOT NULL COMMENT 'Ten nganh hang',
  description VARCHAR(500) COMMENT 'Mo ta',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id),
  UNIQUE KEY uq_nganh_hang_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Danh muc nganh hang.';

-- [F01] mat_bang
CREATE TABLE mat_bang (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  code VARCHAR(50) NOT NULL COMMENT 'Ma mat bang',
  name VARCHAR(200) NOT NULL COMMENT 'Ten mat bang',
  zone_id CHAR(36) NOT NULL COMMENT 'Khu vuc',
  total_area DECIMAL(14,2) NOT NULL COMMENT 'Tong dien tich (m2)',
  type VARCHAR(40) COMMENT 'Loai mat bang (PremiseType)',
  status VARCHAR(40) NOT NULL COMMENT 'Trang thai mat bang (PremiseStatus)',
  location_id CHAR(36) COMMENT 'Vi tri',
  description VARCHAR(1000) COMMENT 'Mo ta',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id),
  UNIQUE KEY uq_mat_bang_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Mat bang cho thue.';

-- [F01] cua_hang
CREATE TABLE cua_hang (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  code VARCHAR(50) NOT NULL COMMENT 'Ma cua hang',
  name VARCHAR(200) NOT NULL COMMENT 'Ten cua hang',
  premise_id CHAR(36) COMMENT 'Mat bang',
  area DECIMAL(14,2) COMMENT 'Dien tich (m2)',
  category_id CHAR(36) COMMENT 'Nganh hang',
  location_id CHAR(36) COMMENT 'Vi tri',
  status VARCHAR(40) NOT NULL COMMENT 'Trang thai cua hang (StoreStatus)',
  operating_hours VARCHAR(200) COMMENT 'Gio hoat dong',
  description VARCHAR(1000) COMMENT 'Mo ta',
  public_visible BOOLEAN NOT NULL DEFAULT 0 COMMENT 'Hien thi cong khai',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id),
  UNIQUE KEY uq_cua_hang_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Cua hang / sap trong TTTM.';

-- [F01] lich_su_mat_bang_cua_hang
CREATE TABLE lich_su_mat_bang_cua_hang (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  target_type VARCHAR(40) COMMENT 'Loai doi tuong (ThamChieuDoiTuong.type)',
  target_id CHAR(36) COMMENT 'Id doi tuong',
  action VARCHAR(100) COMMENT 'Hanh dong',
  old_value_json JSON COMMENT 'Gia tri cu (JSON)',
  new_value_json JSON COMMENT 'Gia tri moi (JSON)',
  changed_by CHAR(36) COMMENT 'Nguoi thay doi',
  reason VARCHAR(500) COMMENT 'Ly do',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  PRIMARY KEY (id),
  KEY ix_ls_mb_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Lich su thay doi mat bang / cua hang.';

-- [F11] hoa_don_mua_hang
CREATE TABLE hoa_don_mua_hang (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  number VARCHAR(50) NOT NULL COMMENT 'So hoa don',
  customer_id CHAR(36) NOT NULL COMMENT 'Khach mua sam',
  store_id CHAR(36) NOT NULL COMMENT 'Cua hang',
  purchased_at DATETIME(6) COMMENT 'Thoi diem mua',
  scanned_at DATETIME(6) COMMENT 'Thoi diem quet',
  total_amount DECIMAL(18,2) NOT NULL COMMENT 'Tong tien',
  attachment_id CHAR(36) COMMENT 'Tep dinh kem',
  verified BOOLEAN NOT NULL DEFAULT 0 COMMENT 'Da xac thuc',
  earned_points BIGINT NOT NULL DEFAULT 0 COMMENT 'Diem tich luy',
  external_reference VARCHAR(200) COMMENT 'Tham chieu ngoai',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id),
  UNIQUE KEY uq_hoa_don_mua_hang_number (number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Hoa don mua hang tai cua hang.';

-- [F10] su_kien
CREATE TABLE su_kien (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  code VARCHAR(50) NOT NULL COMMENT 'Ma su kien',
  name VARCHAR(200) NOT NULL COMMENT 'Ten su kien',
  type VARCHAR(100) COMMENT 'Loai su kien',
  location_id CHAR(36) COMMENT 'Vi tri',
  start_at DATETIME(6) COMMENT 'Bat dau',
  end_at DATETIME(6) COMMENT 'Ket thuc',
  estimated_budget DECIMAL(18,2) COMMENT 'Ngan sach du kien',
  approved_budget DECIMAL(18,2) COMMENT 'Ngan sach duyet',
  target_attendees INT COMMENT 'So nguoi muc tieu',
  status VARCHAR(40) COMMENT 'Trang thai (EventStatus)',
  proposed_by CHAR(36) COMMENT 'Nguoi de xuat',
  approved_by CHAR(36) COMMENT 'Nguoi duyet',
  approved_at DATETIME(6) COMMENT 'Thoi diem duyet',
  rejection_reason VARCHAR(500) COMMENT 'Ly do tu choi',
  public_visible BOOLEAN NOT NULL DEFAULT 0 COMMENT 'Hien thi cong khai',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id),
  UNIQUE KEY uq_su_kien_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Su kien / chuong trinh.';

-- [F10] hoat_dong_su_kien
CREATE TABLE hoat_dong_su_kien (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  name VARCHAR(200) NOT NULL COMMENT 'Ten hoat dong',
  start_at DATETIME(6) COMMENT 'Bat dau',
  end_at DATETIME(6) COMMENT 'Ket thuc',
  location_id CHAR(36) COMMENT 'Vi tri',
  person_in_charge CHAR(36) COMMENT 'Nguoi phu trach',
  description VARCHAR(1000) COMMENT 'Mo ta',
  event_id CHAR(36) NOT NULL COMMENT 'Su kien',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Hoat dong trong su kien.';

-- [F10] chi_phi_thuc_te
CREATE TABLE chi_phi_thuc_te (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  event_id CHAR(36) NOT NULL COMMENT 'Su kien',
  supplier_name VARCHAR(200) COMMENT 'Nha cung cap',
  invoice_number VARCHAR(100) COMMENT 'So hoa don',
  amount DECIMAL(18,2) NOT NULL COMMENT 'So tien',
  incurred_at DATETIME(6) COMMENT 'Thoi diem phat sinh',
  attachment_id CHAR(36) COMMENT 'Tep dinh kem',
  description VARCHAR(1000) COMMENT 'Mo ta',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Chi phi thuc te cua su kien.';

-- [F10] ma_qr_tham_gia
CREATE TABLE ma_qr_tham_gia (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  event_id CHAR(36) NOT NULL COMMENT 'Su kien',
  token_hash VARCHAR(128) NOT NULL COMMENT 'Hash token',
  check_in_url VARCHAR(512) COMMENT 'URL check-in (URI)',
  image_storage_key VARCHAR(300) COMMENT 'Khoa luu anh QR',
  expires_at DATETIME(6) COMMENT 'Het han',
  active BOOLEAN NOT NULL DEFAULT 1 COMMENT 'Dang hoat dong',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Ma QR tham gia su kien.';

-- [F10] luot_tham_gia_su_kien
CREATE TABLE luot_tham_gia_su_kien (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  event_id CHAR(36) NOT NULL COMMENT 'Su kien',
  customer_id CHAR(36) NOT NULL COMMENT 'Khach mua sam',
  qr_code_id CHAR(36) COMMENT 'Ma QR',
  check_in_at DATETIME(6) COMMENT 'Thoi diem check-in',
  scanning_device VARCHAR(200) COMMENT 'Thiet bi quet',
  reward_reference VARCHAR(200) COMMENT 'Tham chieu qua tang',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Luot tham gia su kien.';

-- [F10] chuong_trinh_khuyen_mai
CREATE TABLE chuong_trinh_khuyen_mai (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  code VARCHAR(50) NOT NULL COMMENT 'Ma chuong trinh',
  name VARCHAR(200) NOT NULL COMMENT 'Ten chuong trinh',
  type VARCHAR(40) COMMENT 'Loai khuyen mai (PromotionType)',
  start_at DATETIME(6) COMMENT 'Bat dau',
  end_at DATETIME(6) COMMENT 'Ket thuc',
  minimum_invoice_amount DECIMAL(18,2) COMMENT 'Hoa don toi thieu',
  target_tiers JSON COMMENT 'Hang thanh vien ap dung',
  status VARCHAR(40) COMMENT 'Trang thai (PromotionStatus)',
  terms TEXT COMMENT 'Dieu khoan',
  public_visible BOOLEAN NOT NULL DEFAULT 0 COMMENT 'Hien thi cong khai',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id),
  UNIQUE KEY uq_khuyen_mai_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Chuong trinh khuyen mai (lop cha).';

-- [F10] chuong_trinh_giam_gia
CREATE TABLE chuong_trinh_giam_gia (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  discount_rate DECIMAL(9,4) COMMENT 'Ty le giam',
  maximum_discount DECIMAL(18,2) COMMENT 'Muc giam toi da',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Chuong trinh giam gia (ke thua ChuongTrinhKhuyenMai).';

-- [F10] phieu_uu_dai
CREATE TABLE phieu_uu_dai (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  code VARCHAR(50) NOT NULL COMMENT 'Ma voucher',
  name VARCHAR(200) NOT NULL COMMENT 'Ten voucher',
  discount_type VARCHAR(40) COMMENT 'Loai giam (VoucherDiscountType)',
  value DECIMAL(18,2) COMMENT 'Gia tri giam',
  maximum_discount DECIMAL(18,2) COMMENT 'Muc giam toi da',
  minimum_invoice_amount DECIMAL(18,2) COMMENT 'Hoa don toi thieu',
  issued_limit INT COMMENT 'Gioi han phat hanh',
  issued_count INT NOT NULL DEFAULT 0 COMMENT 'So da phat hanh',
  used_count INT NOT NULL DEFAULT 0 COMMENT 'So da su dung',
  per_customer_limit INT COMMENT 'Gioi han moi khach',
  valid_from DATETIME(6) COMMENT 'Hieu luc tu',
  expires_at DATETIME(6) COMMENT 'Het han',
  active BOOLEAN NOT NULL DEFAULT 1 COMMENT 'Dang hoat dong',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id),
  UNIQUE KEY uq_phieu_uu_dai_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Phieu uu dai / voucher.';

-- [F10] chuong_trinh_tang_phieu
CREATE TABLE chuong_trinh_tang_phieu (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  voucher_id CHAR(36) COMMENT 'Phieu uu dai',
  total_to_issue INT COMMENT 'Tong so phieu phat hanh',
  issued_count INT NOT NULL DEFAULT 0 COMMENT 'So da phat hanh',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Chuong trinh tang voucher (ke thua).';

-- [F10] chuong_trinh_tich_diem
CREATE TABLE chuong_trinh_tich_diem (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  reward_points BIGINT COMMENT 'Diem thuong',
  conversion_amount DECIMAL(18,2) COMMENT 'Gia tri quy doi',
  points_per_conversion INT COMMENT 'So diem moi lan quy doi',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Chuong trinh tich diem (ke thua).';

-- [F10] phieu_uu_dai_khach_hang
CREATE TABLE phieu_uu_dai_khach_hang (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  voucher_id CHAR(36) NOT NULL COMMENT 'Voucher',
  customer_id CHAR(36) NOT NULL COMMENT 'Khach mua sam',
  promotion_id CHAR(36) COMMENT 'Chuong trinh',
  issued_at DATETIME(6) COMMENT 'Thoi diem phat hanh',
  status VARCHAR(40) COMMENT 'Trang thai (VoucherIssueStatus)',
  redeemed_at DATETIME(6) COMMENT 'Thoi diem su dung',
  purchase_invoice_id CHAR(36) COMMENT 'Hoa don mua hang',
  issue_reference VARCHAR(200) COMMENT 'Tham chieu phat hanh',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  updated_at DATETIME(6) COMMENT 'Thoi diem cap nhat gan nhat',
  version BIGINT NOT NULL DEFAULT 0 COMMENT 'Khoa lac quan (optimistic locking)',
  archived_at DATETIME(6) COMMENT 'Thoi diem luu tru (soft delete)',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Phieu uu dai cua khach hang.';

-- [F10] lan_ap_dung_khuyen_mai
CREATE TABLE lan_ap_dung_khuyen_mai (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  promotion_id CHAR(36) NOT NULL COMMENT 'Chuong trinh',
  customer_id CHAR(36) NOT NULL COMMENT 'Khach mua sam',
  invoice_id CHAR(36) NOT NULL COMMENT 'Hoa don mua hang',
  applied_at DATETIME(6) COMMENT 'Thoi diem ap dung',
  discount_amount DECIMAL(18,2) COMMENT 'So tien giam',
  points_granted BIGINT COMMENT 'Diem da cong',
  customer_voucher_id CHAR(36) COMMENT 'Voucher khach hang',
  idempotency_key VARCHAR(120) COMMENT 'Khoa chong trung',
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Thoi diem tao ban ghi',
  created_by CHAR(36) COMMENT 'UUID nguoi tao',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Lan ap dung khuyen mai cho hoa don.';

-- [F10] su_kien_cua_hang
CREATE TABLE su_kien_cua_hang (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  event_id CHAR(36) NOT NULL COMMENT 'Su kien',
  store_id CHAR(36) NOT NULL COMMENT 'Cua hang',
  PRIMARY KEY (id),
  UNIQUE KEY uq_su_kien_cua_hang (event_id, store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Lien ket nhieu-nhieu su kien - cua hang (R041).';

-- [F10] khuyen_mai_cua_hang
CREATE TABLE khuyen_mai_cua_hang (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  promotion_id CHAR(36) NOT NULL COMMENT 'Chuong trinh',
  store_id CHAR(36) NOT NULL COMMENT 'Cua hang',
  PRIMARY KEY (id),
  UNIQUE KEY uq_khuyen_mai_cua_hang (promotion_id, store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Lien ket nhieu-nhieu chuong trinh - cua hang (R039).';

-- [F10] phieu_uu_dai_cua_hang
CREATE TABLE phieu_uu_dai_cua_hang (
  id CHAR(36) NOT NULL COMMENT 'Khoa chinh (UUID)',
  voucher_id CHAR(36) NOT NULL COMMENT 'Voucher',
  store_id CHAR(36) NOT NULL COMMENT 'Cua hang',
  PRIMARY KEY (id),
  UNIQUE KEY uq_phieu_uu_dai_cua_hang (voucher_id, store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Lien ket nhieu-nhieu voucher - cua hang (R040).';

-- =====================================================================
-- FK dung chung (sao chep tu schema.sql; bo FK tenant_id -> khach_thue
-- vi khach_thue nam ngoai baseline - xem docs/decisions/0002)
-- =====================================================================
ALTER TABLE tai_khoan_nguoi_dung ADD CONSTRAINT fk_tai_khoan_nguoi_dung_role_id FOREIGN KEY (role_id) REFERENCES vai_tro(id) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE tai_khoan_nguoi_dung ADD CONSTRAINT fk_tai_khoan_nguoi_dung_customer_id FOREIGN KEY (customer_id) REFERENCES khach_mua_sam(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE tep_dinh_kem ADD CONSTRAINT fk_tep_dinh_kem_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES tai_khoan_nguoi_dung(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE tang ADD CONSTRAINT fk_tang_building_id FOREIGN KEY (building_id) REFERENCES toa_nha(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE khu_vuc ADD CONSTRAINT fk_khu_vuc_floor_id FOREIGN KEY (floor_id) REFERENCES tang(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE vi_tri ADD CONSTRAINT fk_vi_tri_zone_id FOREIGN KEY (zone_id) REFERENCES khu_vuc(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE mat_bang ADD CONSTRAINT fk_mat_bang_zone_id FOREIGN KEY (zone_id) REFERENCES khu_vuc(id) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE mat_bang ADD CONSTRAINT fk_mat_bang_location_id FOREIGN KEY (location_id) REFERENCES vi_tri(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE cua_hang ADD CONSTRAINT fk_cua_hang_premise_id FOREIGN KEY (premise_id) REFERENCES mat_bang(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE cua_hang ADD CONSTRAINT fk_cua_hang_category_id FOREIGN KEY (category_id) REFERENCES nganh_hang(id) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE cua_hang ADD CONSTRAINT fk_cua_hang_location_id FOREIGN KEY (location_id) REFERENCES vi_tri(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE lich_su_mat_bang_cua_hang ADD CONSTRAINT fk_lich_su_mat_bang_cua_hang_changed_by FOREIGN KEY (changed_by) REFERENCES tai_khoan_nguoi_dung(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE hoa_don_mua_hang ADD CONSTRAINT fk_hoa_don_mua_hang_customer_id FOREIGN KEY (customer_id) REFERENCES khach_mua_sam(id) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE hoa_don_mua_hang ADD CONSTRAINT fk_hoa_don_mua_hang_store_id FOREIGN KEY (store_id) REFERENCES cua_hang(id) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE hoa_don_mua_hang ADD CONSTRAINT fk_hoa_don_mua_hang_attachment_id FOREIGN KEY (attachment_id) REFERENCES tep_dinh_kem(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE su_kien ADD CONSTRAINT fk_su_kien_location_id FOREIGN KEY (location_id) REFERENCES vi_tri(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE su_kien ADD CONSTRAINT fk_su_kien_proposed_by FOREIGN KEY (proposed_by) REFERENCES tai_khoan_nguoi_dung(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE su_kien ADD CONSTRAINT fk_su_kien_approved_by FOREIGN KEY (approved_by) REFERENCES tai_khoan_nguoi_dung(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE hoat_dong_su_kien ADD CONSTRAINT fk_hoat_dong_su_kien_location_id FOREIGN KEY (location_id) REFERENCES vi_tri(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE hoat_dong_su_kien ADD CONSTRAINT fk_hoat_dong_su_kien_person_in_charge FOREIGN KEY (person_in_charge) REFERENCES tai_khoan_nguoi_dung(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE hoat_dong_su_kien ADD CONSTRAINT fk_hoat_dong_su_kien_event_id FOREIGN KEY (event_id) REFERENCES su_kien(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE chi_phi_thuc_te ADD CONSTRAINT fk_chi_phi_thuc_te_event_id FOREIGN KEY (event_id) REFERENCES su_kien(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE chi_phi_thuc_te ADD CONSTRAINT fk_chi_phi_thuc_te_attachment_id FOREIGN KEY (attachment_id) REFERENCES tep_dinh_kem(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE ma_qr_tham_gia ADD CONSTRAINT fk_ma_qr_tham_gia_event_id FOREIGN KEY (event_id) REFERENCES su_kien(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE luot_tham_gia_su_kien ADD CONSTRAINT fk_luot_tham_gia_su_kien_event_id FOREIGN KEY (event_id) REFERENCES su_kien(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE luot_tham_gia_su_kien ADD CONSTRAINT fk_luot_tham_gia_su_kien_customer_id FOREIGN KEY (customer_id) REFERENCES khach_mua_sam(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE luot_tham_gia_su_kien ADD CONSTRAINT fk_luot_tham_gia_su_kien_qr_code_id FOREIGN KEY (qr_code_id) REFERENCES ma_qr_tham_gia(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE chuong_trinh_giam_gia ADD CONSTRAINT fk_chuong_trinh_giam_gia_id FOREIGN KEY (id) REFERENCES chuong_trinh_khuyen_mai(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE chuong_trinh_tang_phieu ADD CONSTRAINT fk_chuong_trinh_tang_phieu_id FOREIGN KEY (id) REFERENCES chuong_trinh_khuyen_mai(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE chuong_trinh_tang_phieu ADD CONSTRAINT fk_chuong_trinh_tang_phieu_voucher_id FOREIGN KEY (voucher_id) REFERENCES phieu_uu_dai(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE chuong_trinh_tich_diem ADD CONSTRAINT fk_chuong_trinh_tich_diem_id FOREIGN KEY (id) REFERENCES chuong_trinh_khuyen_mai(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE phieu_uu_dai_khach_hang ADD CONSTRAINT fk_phieu_uu_dai_khach_hang_voucher_id FOREIGN KEY (voucher_id) REFERENCES phieu_uu_dai(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE phieu_uu_dai_khach_hang ADD CONSTRAINT fk_phieu_uu_dai_khach_hang_customer_id FOREIGN KEY (customer_id) REFERENCES khach_mua_sam(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE phieu_uu_dai_khach_hang ADD CONSTRAINT fk_phieu_uu_dai_khach_hang_promotion_id FOREIGN KEY (promotion_id) REFERENCES chuong_trinh_khuyen_mai(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE phieu_uu_dai_khach_hang ADD CONSTRAINT fk_phieu_uu_dai_khach_hang_purchase_invoice_id FOREIGN KEY (purchase_invoice_id) REFERENCES hoa_don_mua_hang(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE lan_ap_dung_khuyen_mai ADD CONSTRAINT fk_lan_ap_dung_khuyen_mai_promotion_id FOREIGN KEY (promotion_id) REFERENCES chuong_trinh_khuyen_mai(id) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE lan_ap_dung_khuyen_mai ADD CONSTRAINT fk_lan_ap_dung_khuyen_mai_customer_id FOREIGN KEY (customer_id) REFERENCES khach_mua_sam(id) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE lan_ap_dung_khuyen_mai ADD CONSTRAINT fk_lan_ap_dung_khuyen_mai_invoice_id FOREIGN KEY (invoice_id) REFERENCES hoa_don_mua_hang(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE lan_ap_dung_khuyen_mai ADD CONSTRAINT fk_lan_ap_dung_khuyen_mai_customer_voucher_id FOREIGN KEY (customer_voucher_id) REFERENCES phieu_uu_dai_khach_hang(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE su_kien_cua_hang ADD CONSTRAINT fk_su_kien_cua_hang_event_id FOREIGN KEY (event_id) REFERENCES su_kien(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE su_kien_cua_hang ADD CONSTRAINT fk_su_kien_cua_hang_store_id FOREIGN KEY (store_id) REFERENCES cua_hang(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE khuyen_mai_cua_hang ADD CONSTRAINT fk_khuyen_mai_cua_hang_promotion_id FOREIGN KEY (promotion_id) REFERENCES chuong_trinh_khuyen_mai(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE khuyen_mai_cua_hang ADD CONSTRAINT fk_khuyen_mai_cua_hang_store_id FOREIGN KEY (store_id) REFERENCES cua_hang(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE phieu_uu_dai_cua_hang ADD CONSTRAINT fk_phieu_uu_dai_cua_hang_voucher_id FOREIGN KEY (voucher_id) REFERENCES phieu_uu_dai(id) ON DELETE CASCADE ON UPDATE RESTRICT;
ALTER TABLE phieu_uu_dai_cua_hang ADD CONSTRAINT fk_phieu_uu_dai_cua_hang_store_id FOREIGN KEY (store_id) REFERENCES cua_hang(id) ON DELETE CASCADE ON UPDATE RESTRICT;
