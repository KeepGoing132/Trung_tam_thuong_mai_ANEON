# D0003 – Hợp đồng API F01

- **Ngày**: 31/10/2026
- **Nguồn**: TK-F01 mục 5 (API chi tiết + bảng tổng hợp), rule `code/rules/04-rest-api-va-validation.md`.

## Quyết định

Baseline lấy mục 5 TK-F01 (bảng tổng hợp 18 endpoint), đúng như rule 04. Các lựa chọn chưa chốt trong Word được chốt tại đây:

- **Phân trang**: `page` mặc định 0, `size` mặc định 20, tối đa 100. Giá trị ngoài 0..100 → 400.
- **Envelope thành công**: `{code, message, data}`; danh sách đặt `content/page/size/totalElements` trong `data`.
- **Envelope lỗi**: `{code, message, errorCode, errors, traceId}`; `errors` chỉ chứa field an toàn.
- **Mã lỗi**: 400 VALIDATION_ERROR; 401 UNAUTHENTICATED (bổ sung); 403 ACCESS_DENIED; 404 RESOURCE_NOT_FOUND; 409 BUSINESS_CONFLICT (trùng mã/vượt diện tích/xung đột hợp đồng/version cũ); 503 DEPENDENCY_UNAVAILABLE; 500 INTERNAL_ERROR.
- **Version lạc quan**: gửi trong body request (`version`), response trả về version mới. Không dùng If-Match/428 (hai thiết kế chưa bắt buộc).
- **Order mặc định ổn định**: sort theo `code` rồi `id` khi không chỉ định; lịch sử sort `created_at` giảm dần rồi `id`.
- **code-availability**: `GET /premises/code-availability?code=&excludeId=` trả `available` boolean, KHÔNG trả lỗi HTTP khi `available=false`; không đếm bản ghi đã lưu trữ (archived) là "đang dùng" — nhưng `UNIQUE(code)` hệ thống không loại archived nên khi `available=true` do mã archived, kết quả vẫn kèm cảnh báo? — Không: trả `available=false` khi mã thuộc bản ghi chưa lưu trữ, và `available=true` chỉ khi không có bản ghi nào (kể cả archived) dùng mã đó, vì UNIQUE thật sự chặn cả archived. Contract test phủ trường hợp archived.
- **PATCH status**: chỉ đổi trạng thái + `reason`; PUT cập nhật không nhận `status` (trạng thái chỉ đổi qua PATCH/sync) — tránh hai lối đổi trạng thái.
- **Lịch sử**: chỉ có GET; không API sửa/xóa.

## Cấu trúc file

`docs/openapi/aneon-f01-f10-openapi.yaml` mô tả đủ 18 endpoint; thay đổi hợp đồng phải sửa file này cùng test.
