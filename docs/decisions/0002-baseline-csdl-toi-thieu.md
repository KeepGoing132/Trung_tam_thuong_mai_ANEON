# D0002 – Baseline CSDL tối thiểu cho F01/F10

- **Ngày**: 31/10/2026
- **Nguồn**: rule `code/rules/03-co-so-du-lieu-va-giao-dich.md`, `../schema.sql`.

## Quyết định

Chọn phương án "baseline tối thiểu có các bảng tham chiếu thật sự cần": Flyway V1 tạo đúng cấu trúc (cột, kiểu, unique, FK) của 8 bảng F01 và 15 bảng F10, cùng 5 bảng dùng chung được tham chiếu thật sự bởi FK:

- `tai_khoan_nguoi_dung` + `vai_tro` (changed_by/proposed_by/approved_by/person_in_charge)
- `tep_dinh_kem` (chi_phi_thuc_te.attachment_id)
- `khach_mua_sam`, `hoa_don_mua_hang` (luot_tham_gia, phieu_uu_dai_khach_hang, lan_ap_dung)

Cấu trúc sao chép nguyên văn từ `../schema.sql` (InnoDB, utf8mb4, CHAR(36), DATETIME(6), DECIMAL đúng scale). Không tạo `CREATE DATABASE/USE`; tên database do cấu hình datasource quyết định. Không bỏ FK có thật; FK trỏ ra ngoài tập baseline (`tai_khoan_nguoi_dung.tenant_id → khach_thue`) không đưa vào vì khach_thue không thuộc tập cần để chạy F01/F10 — ghi rõ ở đây thay vì im lặng.

Không nạp `../seed.sql` làm dữ liệu nghiệp vụ chuẩn (rule 00: seed là dữ liệu mẫu chưa khớp mô hình).

## Bổ sung ràng buộc (V2)

Schema gốc thiếu ràng buộc cho các bất biến F10; V2 thêm:

- `ma_qr_tham_gia`: cột sinh `active_guard` = NULL khi `active=0`, = 1 khi `active=1`; UNIQUE (`event_id`, `active_guard`) bảo đảm **một QR active mỗi sự kiện** ở mức DB.
- `luot_tham_gia_su_kien`: UNIQUE (`event_id`, `customer_id`) — mỗi khách một lượt check-in mỗi sự kiện (đề xuất được nhận, xem D0007).
- `lan_ap_dung_khuyen_mai`: UNIQUE (`idempotency_key`) khi khác NULL (cột sinh NOT-NULL-guard) — chống áp dụng trùng lặp.
- `phieu_uu_dai_khach_hang`: UNIQUE (`voucher_id`, `issue_reference`) khi khác NULL — idempotency phát voucher theo tham chiếu.
- `hoat_dong_su_kien`: UNIQUE (`location_id`, `start_at`, `end_at`, `active_guard`) là **phòng thủ thêm**, không thay thế kiểm tra giao khoảng trong service (không phát hiện trùng một phần).

Việc thêm index này nằm trong rà soát bắt buộc của rule 03.
