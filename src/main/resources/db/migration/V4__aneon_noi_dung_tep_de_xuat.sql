CREATE TABLE noi_dung_tep_de_xuat (
  attachment_id CHAR(36) NOT NULL,
  contents LONGBLOB NOT NULL,
  PRIMARY KEY (attachment_id),
  CONSTRAINT fk_noi_dung_tep_de_xuat FOREIGN KEY (attachment_id) REFERENCES tep_dinh_kem(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
