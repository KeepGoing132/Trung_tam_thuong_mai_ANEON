# D0008 – Danh tính, quyền và phạm vi dữ liệu

- **Ngày**: 31/10/2026
- **Nguồn**: rule `code/rules/05-bao-mat-va-phan-quyen.md`, rule `code/rules/06-nghiep-vu-aneon.md`, TK-F01/TK-F10 (ROLE_... là tên đề xuất).

## Quyết định

- **Danh tính**: Spring Security resource server nhận JWT (issuer/audience xác định bằng cấu hình). **Chưa có identity provider** → profile `local` dùng cơ chế nhận diện header (`X-Actor-Id`, `X-Actor-Roles`) gắn nhãn rõ là stub local, tắt hoàn toàn ở profile khác; production không tự cấp quyền.
- **Quyền chức năng**: kiểm tra theo authority dạng `f01:premise:read`, `f01:premise:write`, `f01:store:write`, `f01:status:write`, `f01:history:read`, `f01:utilization:read`, `f10:event:propose`, `f10:event:approve`, `f10:event:schedule`, `f10:event:qr`, `f10:event:report`, `f10:expense:write`, `f10:checkin:assist`, `f10:promotion:write`, `f10:voucher:write`, `f10:promotion:read`, `f10:voucher:issue`. Đây là tên đề xuất; khi F16 giao ma trận quyền thật thì thay bằng mã của F16.
- **Mapping vai trò đề xuất** (RoleCode chung → quyền): MANAGEMENT = BQL (f01 write/read/status/history, f10 promotion/voucher write); OPERATIONS = VHKT (f01 read/status/utilization); DIRECTOR = GĐ (f01 utilization + read); MARKETING = MKT staff (f10 propose/schedule/qr/report/promotion read); SYSTEM_ADMIN không nhận quyền kinh doanh (không duyệt ngân sách, không duyệt sự kiện); TENANT/CUSTOMER chỉ public search.
  - Duyệt sự kiện (`f10:event:approve`): quản lý Marketing trở lên → MANAGEMENT và DIRECTOR.
  - MKT_STAFF/MANAGER/ADMIN trong Word là alias chưa thống nhất — không tạo role đồng nghĩa, map như trên và ghi vào đây.
- **Scope dữ liệu**: `AccessScopePort` trả phạm vi của actor (danh sách zone được phép hoặc ALL). Kiểm tra trong SQL trước phân trang cho list/count/summary/history/report. Public search không cần scope. Local stub cấp ALL cho nhân viên nội bộ — có nhãn, chỉ profile local/test.
- **Public search**: chỉ `publicVisible=true`, chưa `archived`, sự kiện APPROVED (sắp/đang diễn ra), khuyến mãi SCHEDULED/ACTIVE; không trả ngân sách, chi phí, người duyệt, QR, dữ liệu khách.
- **Audit**: các thao tác sửa mặt bằng/chuyển trạng thái/duyệt sự kiện/đổi hạn mức/thao tác thay khách đều để lại vết (lịch sử F01 hoặc log audit có traceId) — không sửa/xóa lịch sử.
