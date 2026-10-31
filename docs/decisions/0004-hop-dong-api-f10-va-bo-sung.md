# D0004 – Hợp đồng API F10 và endpoint bổ sung

- **Ngày**: 31/10/2026
- **Nguồn**: TK-F10 mục 5 + bảng tổng hợp 9 endpoint, rule `code/rules/04-rest-api-va-validation.md`, rule `code/rules/f10-su-kien-va-khuyen-mai.md`.

## Quyết định

Baseline: 9 endpoint của bảng tổng hợp cuối TK-F10 (`/approval`, `/activities`, `/qr-code`, `/report`, `/realtime`, `/promotions-events/search`…), không mở alias `/approve`, `/schedules` như annotation phần đầu Word.

Các thao tác hỗ trợ bị thiếu trong 9 endpoint nhưng cần để hoàn tất use case, được **bổ sung có đánh dấu** (response header `X-Contract-Extension` không dùng; đánh dấu bằng section `Supplemental` trong OpenAPI và quyết định này):

| Endpoint | Lý do bổ sung |
| --- | --- |
| GET /events/{id} | Chi tiết sự kiện — TK-F10 liệt kê ở lớp controller nhưng thiếu trong 9 API. |
| POST /events/{id}/expenses | Ghi chi phí thực tế — F10UC01.4 cần số liệu thật; phương thức `recordActualExpense` đã có ở lớp service. |
| POST /events/{id}/check-ins | Check-in qua QR — F10UC01.3/01.4 và `validateAndProcessCheckin` đã có ở lớp service. |
| POST /vouchers/{id}/issues | Phát voucher cho khách — F10UC02.2/02.3, `issueVoucherToCustomer` đã có ở lớp service. |
| POST /voucher-instances/{id}/redemptions | Dùng voucher — chuyển ISSUED→USED có điều kiện; tránh tự thêm DELETE/CRUD. |
| POST /promotions/{id}/applications | Áp dụng khuyến mãi cho hóa đơn — `applyPromotionToOrder` đã có ở lớp service. |

## Lựa chọn hợp đồng

- **Phân trang**: như D0003 (`page=0`, `size=20`, max 100). Public search gộp EVENT/PROMOTION: phân trang hợp nhất một lần trong query, sort ổn định (`startAt`/`endAt` rồi `id`), totalElements tính trên tập hợp nhất.
- **Trạng thái trả về khi tạo đề xuất**: `PENDING_APPROVAL` (gửi luôn, không có bước lưu nháp riêng trong 9 API; D0001 liệt kê DRAFT nhưng không mở API lưu nháp).
- **Duyệt**: `action=APPROVE` bắt buộc `approvedBudget > 0`; `action=REJECT` bắt buộc `rejectionReason`. Response trả mã nội bộ `APPROVED`/`REJECTED` (không dùng nhãn Word DA_DUYET/CHO_DUYET — xem D0001).
- **Tỷ lệ giảm giá**: API nhận phần trăm 0..100 (ví dụ 15 nghĩa là 15%); lưu DB DECIMAL(9,4) cũng theo thang 0..100 (`discount_rate=15.0000`). Không nhận 0.15.
- **`issuedLimit`**: 0 = không giới hạn (không phải hết kho); không âm.
- **QR**: response trả `checkInUrl` và `imageStorageKey` chỉ cho actor có quyền sự kiện (rule 00: không lộ key trong DTO công khai); token không bao giờ trả nguyên bản (chỉ qua URL check-in dùng một lần cho QR active).
- **Realtime (F10UC02.3)**: số liệu lấy từ MySQL (issued_count/used_count, tổng `discount_amount`/`points_granted` từ lan_ap_dung). Không dựng Redis trong phiên bản này (D0006); `lastUpdated` = thời điểm query.
- **Báo cáo**: `attendanceRate` chỉ trả khi `targetAttendees > 0`, đơn vị phần trăm (số 0..n); `roiPercentage` null khi chưa xác định được doanh thu quy thuộc hoặc chi phí = 0 (không nhân số mẫu 24.5%).
- **Tệp đề xuất**: `proposalFileKeys` phải trỏ tới tệp đã tải lên bởi cùng actor, phần mở rộng PDF/DOCX/PNG/JPG, ≤ 25 MB (25 × 1024 × 1024 byte — chốt đơn vị là MiB), tối đa 10 tệp mỗi đề xuất (cấu hình bổ sung, ghi tại đây).
- **personInCharge**: nhận UUID tài khoản; sai/không tồn tại → 400/404 theo nguồn lookup. Không nhận tên người.
