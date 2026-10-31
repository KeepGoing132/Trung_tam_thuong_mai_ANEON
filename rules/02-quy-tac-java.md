# Quy tắc Java cho F01 và F10

## Tên và tổ chức

Class PascalCase, method/field camelCase, constant UPPER_SNAKE_CASE, package chữ thường. Entity giữ tên miền Việt không dấu theo schema/mô hình.

F01 dùng MatBangService, CuaHangSapService, TrangThaiMatBangService, LichSuMatBangService, KhaiThacMatBangService. F10 dùng EventService, PromotionService, QRCodeService, VoucherService, EventReportService theo thiết kế; không đổi tên dây chuyền để hai module giống nhau.

DTO theo ý định: CreatePremiseRequest, UpdatePremiseRequest, ApproveEventRequest, CreateVoucherRequest… Không dùng một DTO chứa mọi field cho tạo/sửa/duyệt. Một class một trách nhiệm, import tường minh, không wildcard. Comment giải thích bất biến/lý do khó thấy. Thông báo/tài liệu tiếng Việt; JSON/API/mã lỗi theo contract.

## Dữ liệu

| Loại | Quy tắc |
| --- | --- |
| ID | UUID trong Java, JSON UUID chuẩn, CHAR(36) theo SQL |
| Tiền/diện tích/tỷ lệ | BigDecimal, precision/scale theo cột; không float/double dù ví dụ Word dùng Double |
| Thời điểm | Instant cho startAt/endAt/createdAt; zone nghiệp vụ khi cần |
| Ngày không có giờ | LocalDate khi đúng ý nghĩa trường |
| Version | Long/long theo BIGINT, không ép xuống int |
| Trạng thái/loại | Enum với mapping đã quyết định, không ordinal |

Phân biệt missing/null của DTO; primitive không che mất field bắt buộc bằng default. Với phần trăm, ghi rõ API dùng 15 hay 0.15 và chuyển đổi một lần, không đoán từ discount_rate.

## Cài đặt

Constructor injection, dependency final khi phù hợp. DTO có thể là record; JPA entity có constructor phù hợp, không record. Tránh Lombok @Data trên entity lazy vì equals/toString dễ truy cập dữ liệu/vòng lặp. Mapper chỉ gán field allowlist; client không đặt actor/version/ngân sách duyệt/bộ đếm/tổng báo cáo.

BigDecimal dùng compareTo khi so giá trị, không tạo từ double. Làm tròn/tỷ lệ ghi rõ theo chính sách, không rải HALF_UP tùy tiện. Collection rỗng thay null; Optional cho kết quả tra cứu và xử lý trường hợp rỗng.

Không mutable state theo request trong singleton. Không parallelStream/thread tự tạo/@Async để né transaction. Sort ổn định có id. Không tải toàn bảng để tổng hợp/phân trang trong Java khi DB thực hiện đúng scope được.

## Ngoại lệ và log

Phân biệt validation/không tìm thấy/thiếu quyền/xung đột/hạ tầng. Runtime domain exception hoặc rollbackFor cho checked exception sau ghi. Không catch Exception trả success/null; giữ cause khi chuyển ngoại lệ.

Logger tham số hóa có traceId; không System.out.println hoặc log JWT/password/QR token/URL token/raw dữ liệu khách. Không TODO, response hard-code hoặc UnsupportedOperationException trên luồng báo hoàn tất. Stub chỉ ở test/profile local có nhãn.
