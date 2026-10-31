# REST API và validation F01 và F10

## Baseline

Thiết kế Word là baseline đề xuất, chưa có ứng dụng/hợp đồng máy đọc đã xác nhận trong repository. Trước khi code endpoint, lập OpenAPI hoặc contract tương đương trong code/ từ API chi tiết hai Word. Hợp đồng phải truy vết về đúng use case F01/F10.

Base /api/v1/f01 và /api/v1/f10; JSON UTF-8 camelCase, UUID chuẩn, thời điểm ISO 8601 có timezone/UTC. Success theo mẫu {code,message,data}; HTTP status/code nhất quán. Danh sách có content/page/size/totalElements trong data khi phù hợp, không đổi sang envelope module khác. Đề xuất page=0, size=20, max=100 phải ghi contract vì ví dụ Word chưa chốt default.

## API F01

Path sau /api/v1/f01. Bộ lọc dùng chung, không tạo ba endpoint riêng.

| Method/path | Use case |
| --- | --- |
| GET /premises; GET /premises/{id} | F01UC01.1 |
| POST /premises | F01UC01.2 |
| PUT /premises/{id} | F01UC01.3 |
| GET /premises/code-availability?code=&excludeId= | F01UC01.4 |
| PUT /premises/{id}/location | F01UC01.5 |
| GET /stores; GET /stores/{id} | F01UC02.1 |
| POST /stores | F01UC02.2 |
| PUT /stores/{id} | F01UC02.3 |
| PATCH /stores/{id}/status | F01UC02.4 |
| GET /premises/statuses | F01UC03.1 |
| PATCH /premises/{id}/status | F01UC03.2 |
| POST /premises/{id}/sync-contract-status | F01UC03.3 |
| GET /premises/{id}/history | F01UC04.1 |
| GET /histories | F01UC04.2 |
| GET /utilization, query zoneId/floorId/status | F01UC05.1–05.4 |

Phân biệt path tĩnh /statuses, /code-availability với /{id}. Availability không lộ dữ liệu ngoài scope và không báo true trái UNIQUE toàn hệ thống.

## API F10

Ưu tiên mục 5/bảng tổng hợp cuối Word vì annotation trước đó có path khác.

| Method/path sau /api/v1/f10 | Use case |
| --- | --- |
| POST /events/proposals | F10UC01.1 |
| PUT /events/{id}/approval | F10UC01.5 |
| POST /events/{id}/activities | F10UC01.2 |
| POST /events/{id}/qr-code | F10UC01.3 |
| GET /events/{id}/report | F10UC01.4 |
| POST /promotions | F10UC02.1 |
| POST /vouchers | F10UC02.2 |
| GET /promotions/{id}/realtime | F10UC02.3 |
| GET /promotions-events/search | F10UC03 |

Check-in, issue/redeem/apply, ghi chi phí, export, detail được nhắc ở lớp/luồng nhưng chưa đủ đặc tả trong 9 API trên. Khi nhiệm vụ cần, bổ sung DTO/path/quyền/idempotency/lỗi rõ và đánh dấu contract bổ sung. Không bỏ kiểm soát kho vì thiếu API; cũng không tự làm mọi thao tác hỗ trợ khi chỉ được giao một UC.

## DTO và validation

Controller validate cấu trúc; service kiểm tra bất biến/tham chiếu/scope/trạng thái. DTO allowlist không nhận actor/audit/proposedBy/approvedBy/bộ đếm/báo cáo hoặc approvedBudget qua tạo đề xuất. Chọn chính sách field lạ/required/null rồi ghi contract.

Kiểm tra code/name, UUID, độ dài theo schema; số tiền/diện tích dương hoặc không âm theo trường; startAt < endAt; hoạt động nằm trong event; tỷ lệ đúng đơn vị; cửa hàng và subtype hợp lệ. personInCharge chuyển đúng UUID tham chiếu, không ghi tên vào cột UUID. Key tệp phải thuộc tệp đã tải hợp lệ và có quyền.

GET không gây side effect check-in/redeem, dùng command có xác thực cho thay đổi đó. PATCH chỉ sửa field được phép; PUT không tự là patch mọi field. Không thêm DELETE/CRUD lịch sử ngoài UC.

Version qua request field hay If-Match là quyết định bổ sung phải ghi thống nhất. Hai thiết kế chưa bắt buộc strong ETag, 428 hoặc TTL idempotency 24 giờ; không nhận là yêu cầu gốc.

## Lỗi

Baseline F01: 200 đọc/cập nhật, 201 tạo; 400 VALIDATION_ERROR; 403 ACCESS_DENIED gồm ngoài phạm vi theo thiết kế; 404 RESOURCE_NOT_FOUND; 409 BUSINESS_CONFLICT cho code/diện tích/hợp đồng/version; 503 DEPENDENCY_UNAVAILABLE khi không xác minh được phụ thuộc bắt buộc. Bổ sung 401 chưa xác thực, 500 lỗi bất ngờ. F10 giữ mã cụ thể như SCHEDULE_CONFLICT.

Đề xuất lỗi {code,message,errorCode,errors,traceId}; errors chứa field an toàn. Ghi đây là chuẩn hóa triển khai do Word chưa đủ schema mọi lỗi. Nếu chọn ngoài scope 404 thay 403, cập nhật contract/test rõ, không trộn chính sách list/detail/file.

Không trả 200 cho lỗi nghiệp vụ hoặc lộ SQL/stack trace/secret/redisStockKey/raw QR trong response công khai. Security filter dùng AuthenticationEntryPoint/AccessDeniedHandler cùng format, không trông chờ ControllerAdvice bắt lỗi trước Controller.
