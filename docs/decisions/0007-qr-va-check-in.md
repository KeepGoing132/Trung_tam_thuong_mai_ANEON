# D0007 – QR check-in và bổ sung hợp đồng check-in

- **Ngày**: 31/10/2026
- **Nguồn**: rule `code/rules/f10-su-kien-va-khuyen-mai.md` (QR và check-in), rule `code/rules/05-bao-mat-va-phan-quyen.md` (Tệp và QR), TK-F10.

## Quyết định

- **Token**: sinh bằng `SecureRandom` 32 byte, mã hóa base64url; DB chỉ lưu SHA-256 (`token_hash`), không lưu token gốc. `check_in_url` = `{cấu hình public base URL}?token={token gốc}` — URL này chỉ trả trong response của `POST /events/{id}/qr-code` cho actor có quyền sự kiện, không log, không nằm trong DTO công khai.
- **Một QR active mỗi sự kiện**: sinh lại QR = thu hồi mã cũ (`active=false`) rồi tạo mã mới trong cùng transaction; UNIQUE (`event_id`, `active_guard`) chặn hai request tạo hai mã active (V2 migration).
- **Hiệu lực**: `expiresAt` mặc định = `endAt` của sự kiện (không nới quá), có thể đặt sớm hơn; check-in ngoài `expiresAt`, ngoài `startAt..endAt`, hoặc sự kiện không `APPROVED/ONGOING` → 409/400 theo trường hợp.
- **Check-in**: `POST /events/{id}/check-ins` (bổ sung, xem D0004). Khách xác thực check-in cho chính mình (`customerId` lấy từ principal); nhân viên hỗ trợ phải có quyền `f10:checkin:assist` và gửi `customerId` rõ ràng — ghi audit.
- **Chống lặp**: UNIQUE (`event_id`, `customer_id`) — mỗi khách một lượt mỗi sự kiện (đề xuất được nhận; tái nhập nhiều lượt cần quyết định nghiệp vụ riêng). Quét đồng thời: request thứ hai chạm ràng buộc → trả kết quả lượt đầu (idempotent), không cộng thưởng lần hai.
- **Thưởng**: nếu sự kiện cấu hình thưởng, ghi `reward_reference`; cộng điểm F11 thực hiện sau commit qua `LoyaltyPort` có outbox/retry — thất bại provider không làm mất lượt check-in, không trả "đã cộng điểm" khi mới xếp hàng.
