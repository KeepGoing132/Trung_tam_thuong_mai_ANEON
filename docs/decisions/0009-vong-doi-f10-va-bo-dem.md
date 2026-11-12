# D0009 – Vòng đời sự kiện/khuyến mãi, làm tròn và bộ đếm

- **Ngày**: 31/10/2026
- **Nguồn**: rule `code/rules/f10-su-kien-va-khuyen-mai.md`, TK-F10, schema.

## Vòng đời sự kiện

- Tạo đề xuất = gửi duyệt: `PENDING_APPROVAL` (không mở API lưu nháp riêng trong 9 API baseline).
- `PENDING_APPROVAL` → `APPROVED` (bắt buộc `approvedBudget > 0`, ghi người/thời điểm) hoặc `REJECTED` (bắt buộc lý do). Chỉ pending được duyệt/từ chối; hai quản lý duyệt cạnh tranh chỉ một kết quả commit qua version + khóa dòng.
- Xếp lịch hoạt động cho phép khi `PENDING_APPROVAL` hoặc `APPROVED`. Trạng thái "giữ chỗ" sảnh: `PENDING_APPROVAL`, `APPROVED`, `ONGOING` — `REJECTED`/`CANCELLED`/`FINISHED`/`DRAFT` không chiếm sảnh.
- Sinh QR cho phép khi `APPROVED`/`ONGOING`. Check-in trong `[startAt, endAt)` và QR còn hạn/active.
- Chuyển `ONGOING`/`FINISHED`/`CANCELLED` chưa mở API trong baseline (cần đặc tả trước khi mở, rule f10).

## Vòng đời khuyến mãi

- Trạng thái ban đầu khi tạo: `SCHEDULED` nếu chưa tới hạn, `ACTIVE` nếu đang trong khoảng, `FINISHED` nếu đã quá hạn. Không nhận `DANG_DIEN_RA` (D0001).
- Khi áp dụng, xác định hiệu lực của chương trình `ACTIVE` hoặc `SCHEDULED` theo `[startAt, endAt]`. Chương trình đặt lịch có hiệu lực từ giờ bắt đầu mà không cần tác vụ nền cập nhật trạng thái đã lưu.

## Làm tròn và đơn vị

- Phần trăm giảm giá: API và DB đều theo thang 0..100 (`discount_rate = 15.0000` là 15%).
- Số tiền giảm làm tròn HALF_UP, 2 chữ số thập phân (mức DECIMAL(18,2)).
- Điểm: `POINT_ACCUMULATION` cộng `rewardPoints` cho mỗi hóa đơn đủ điều kiện (quy đổi chỉ dùng hiển thị). Đây là cách đọc được chọn; nghiệp vụ cần chốt nếu muốn tích theo bậc quy đổi.

## Bộ đếm và idempotency

- `issued_count`/`used_count` của `phieu_uu_dai` là bộ đếm nguồn; UPDATE có điều kiện (`issued_count < issued_limit` hoặc unlimited) — 0 dòng cập nhật = hết kho.
- `chuong_trinh_tang_phieu.issued_count` không dùng làm bộ đếm phát (giữ nguyên theo schema); realtime của loại VOUCHER_GIVEAWAY đọc bộ đếm voucher được liên kết.
- Idempotency: `lan_ap_dung_khuyen_mai.idempotency_key` (UNIQUE V2) cho apply; `(voucher_id, issue_reference)` cho phát; trạng thái `ISSUED→USED` cho redeem; `UNIQUE(event_id, customer_id)` cho check-in.

## Ghi chú cấu trúc

- QrCodeController gộp vào EventController vì mọi route QR đều `/events/{id}/qr-code` — hợp đồng URL không đổi so với bảng tổng hợp TK-F10.
- Thưởng check-in không tự bịa: `reward_reference` chỉ ghi tham chiếu khi có cấu hình thưởng từ nghiệp vụ (chưa có nguồn → không cấp điểm tự động).
