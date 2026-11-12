ALTER TABLE tai_khoan_nguoi_dung ADD COLUMN password_change_required BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE phien_dang_nhap (
  token_hash CHAR(64) NOT NULL,
  account_id CHAR(36) NOT NULL,
  auth_version BIGINT NOT NULL,
  expires_at DATETIME(6) NOT NULL,
  PRIMARY KEY (token_hash),
  KEY ix_phien_account (account_id),
  KEY ix_phien_expiry (expires_at),
  CONSTRAINT fk_phien_account FOREIGN KEY (account_id) REFERENCES tai_khoan_nguoi_dung(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ma_khoi_phuc_tai_khoan (
  account_id CHAR(36) NOT NULL,
  code_hash CHAR(64) NOT NULL,
  PRIMARY KEY (account_id),
  CONSTRAINT fk_khoi_phuc_account FOREIGN KEY (account_id) REFERENCES tai_khoan_nguoi_dung(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE gioi_han_dang_nhap (
  bucket CHAR(64) NOT NULL,
  attempts INT NOT NULL,
  expires_at DATETIME(6) NOT NULL,
  PRIMARY KEY (bucket),
  KEY ix_gioi_han_expiry (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO vai_tro (id, code, name) VALUES
  ('f1600000-0000-4000-8000-000000000001', 'SYSTEM_ADMIN', 'Quản trị hệ thống'),
  ('f1600000-0000-4000-8000-000000000002', 'MANAGEMENT', 'Ban quản lý'),
  ('f1600000-0000-4000-8000-000000000003', 'DIRECTOR', 'Giám đốc'),
  ('f1600000-0000-4000-8000-000000000004', 'OPERATIONS', 'Vận hành'),
  ('f1600000-0000-4000-8000-000000000005', 'ACCOUNTING', 'Kế toán'),
  ('f1600000-0000-4000-8000-000000000006', 'MARKETING', 'Marketing'),
  ('f1600000-0000-4000-8000-000000000007', 'CUSTOMER', 'Khách mua sắm')
ON DUPLICATE KEY UPDATE id = vai_tro.id;
