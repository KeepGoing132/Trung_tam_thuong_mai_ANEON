# Bảo mật F01 và F10

## Danh tính và quyền

F16 là phụ thuộc ngoài phạm vi triển khai đầy đủ. Dùng AccessScopePort/security adapter lấy actor/quyền/scope, test có principal giả. Identity provider chưa có thì profile local ghi nhãn; production không tự cấp toàn quyền.

Spring Security kiểm tra JWT chữ ký/thuật toán cho phép/issuer/audience/thời hạn theo hợp đồng tích hợp. Không decode rồi tin payload. Actor lấy từ danh tính xác thực, không tin actorId/proposedBy/approvedBy trong JSON.

RoleCode chung: DIRECTOR, MANAGEMENT, OPERATIONS, ACCOUNTING, MARKETING, TENANT, CUSTOMER, SYSTEM_ADMIN. Alias Word MARKETING_STAFF/MKT_STAFF/MANAGER/ADMIN chưa thống nhất: map sang quyền thật, không tạo role đồng nghĩa. ServiceStaff cần quyền chức năng cụ thể; SYSTEM_ADMIN không tự được duyệt ngân sách.

| Chức năng | Tác nhân/điều kiện theo nguồn |
| --- | --- |
| F01 tạo/sửa mặt bằng/cửa hàng/vị trí | Ban quản lý, quyền thao tác và scope |
| F01 xem/cập nhật/đồng bộ trạng thái | Ban quản lý/vận hành theo UC và quyền; kiểm tra hợp đồng |
| F01 lịch sử/khai thác | Ban quản lý/vận hành/Giám đốc theo UC và scope |
| F10 đề xuất/xếp lịch/QR | Marketing có quyền và scope |
| F10 duyệt/từ chối/cấp ngân sách | Quản lý Marketing trở lên theo phân tích, quyền duyệt |
| F10 cấu hình khuyến mãi/voucher | Quản lý; Marketing khi có quyết định giao quyền rõ |
| F10 báo cáo/realtime | Marketing/Quản lý đúng scope, không public ngân sách/dữ liệu khách |
| F10 public search | Khách/nhân viên chỉ xem dữ liệu public đủ điều kiện |
| Check-in/issue/redeem nếu triển khai | Khách xác thực cho chính mình, hoặc nhân viên được cấp quyền hỗ trợ và có audit |

Permission cụ thể theo F16; tên mới phải ghi là đề xuất, không nhận là đã có. Mặc định chặn endpoint chưa khai báo quyền.

## Scope

Scope trong SQL trước phân trang, gồm list/count/summary/detail/history/report/file/export. F10 kiểm tra participatingStoreIds/locationId trong vùng được phép và dữ liệu F01 hợp lệ. F01 đổi vùng/vị trí kiểm tra quyền cũ/mới theo chính sách đã chọn.

Public search chỉ publicVisible=true, chưa archived, trạng thái/hiệu lực cho phép. Không trả nháp/chờ duyệt/từ chối/hủy/đã kết thúc, chi phí/ngân sách/người duyệt/QR secret/dữ liệu khách. Quyền xem public không phải quyền check-in/phát voucher cho customerId bất kỳ.

## Tệp và QR

Phân tích F10 cho **PDF, DOCX, PNG, JPG, không quá 25 MB/file**. Kiểm tra nội dung/MIME và phần mở rộng; giới hạn số file/tổng request là cấu hình bổ sung cần ghi rõ. Làm rõ đơn vị byte của 25 MB trong contract.

Storage key sinh phía server, chống path traversal, kiểm tra sở hữu/quyền đọc, không nhận key của người khác. Binary tách metadata, staging/cleanup khi DB rollback. File riêng tư tải qua URL có thời hạn hoặc endpoint có kiểm tra quyền.

QR token khó đoán bằng SecureRandom, lưu hash/HMAC theo quyết định, không dùng eventId làm token. URL/ảnh QR có token cần quyền/hạn dùng/thu hồi, không log hoặc public search. Nếu lưu check_in_url đầy đủ theo schema, coi đây là bí mật tương đương raw token, bảo vệ quyền đọc/lưu trữ.

## Cấu hình và audit

Secret/JWT key/Redis password/DB password qua môi trường/secret store, không Git/seed/example. Query tham số hóa, filter/sort allowlist. CORS origin/header đúng nhu cầu, không wildcard với credentials. CSRF cấu hình theo bearer header hoặc cookie thật, không tắt theo thói quen.

Rate limit search/check-in/voucher/upload cần chốt như cấu hình đề xuất, không có ngưỡng gốc đã xác nhận trong F01/F10. Log traceId và kết quả an toàn. Audit sửa mặt bằng/chuyển trạng thái/duyệt sự kiện/đổi hạn mức/thao tác thay khách; không sửa xóa lịch sử để che thay đổi.

Tham khảo: [Spring Security authorization](https://docs.spring.io/spring-security/reference/servlet/authorization/index.html), dùng tài liệu đúng phiên bản build.
