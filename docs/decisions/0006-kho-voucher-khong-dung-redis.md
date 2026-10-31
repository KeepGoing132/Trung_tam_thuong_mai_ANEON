# D0006 – Kho voucher: MySQL là nguồn bền vững, không dùng Redis

- **Ngày**: 31/10/2026
- **Nguồn**: rule `code/rules/03-co-so-du-lieu-va-giao-dich.md` (Voucher, Redis và side effect), rule `code/rules/00-boi-canh-va-nguon.md` (Redis DECR vs MySQL).

## Quyết định

Không triển khai Redis trong phiên bản này. MySQL là nguồn bền vững duy nhất cho tồn kho voucher và bộ đếm:

- Phát voucher: khóa dòng `phieu_uu_dai` (`SELECT ... FOR UPDATE`), kiểm `issued_limit` (0 = không giới hạn) và `per_customer_limit`, INSERT `phieu_uu_dai_khach_hang`, UPDATE `issued_count = issued_count + 1` — tất cả trong một transaction.
- Dùng voucher: khóa dòng voucher, chuyển trạng thái bản ghi khách `ISSUED → USED` có điều kiện, tăng `used_count`.
- Áp dụng khuyến mãi: INSERT `lan_ap_dung_khuyen_mai` với `idempotency_key` UNIQUE; dữ liệu hóa đơn xác minh qua port F11, không tin tiền client khai.

Vì lý do chọn cách này: Redis DECR là giao dịch riêng, không gộp được với MySQL; dùng DB trực tiếp loại trừ lớp reserve/compensate/reconcile chưa có yêu cầu. TK-F10 nhắc Redis/Spring Cache như lựa chọn thiết kế, rule 01 ghi rõ không là nguồn duy nhất của tồn kho.

## Hệ quả

- `GET /promotions/{id}/realtime` trả số liệu từ DB, độ "real-time" là thời điểm query.
- Khi tải cao cần Redis/cache, bổ sung theo mô hình reserve + compensation của rule 03 và cập nhật quyết định này.
