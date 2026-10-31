# Bối cảnh và nguồn cho F01 và F10

## Phạm vi

Người dùng chỉ làm F01 (F1) và F10 trong hệ thống quản lý Trung tâm Thương mại Thông minh ANEON, bằng Java Spring Boot. Module khác chỉ là nguồn tham chiếu/phụ thuộc tích hợp.

Khi lập rule ngày 31/10/2026, code/ là repository riêng, ban đầu chỉ có README; chưa có Java hoặc build. ../schema.sql dùng MySQL 8.0, InnoDB, utf8mb4, database qldapm_aneon, 109 bảng toàn hệ thống; F01 có 8 bảng, F10 có 15 bảng. Không triển khai toàn bộ hệ thống theo số bảng.

## Tài liệu cần đọc

Đường dẫn tính từ code/. Đọc cả bảng, luồng thay thế, ngoại lệ và ràng buộc. Trích XML DOCX chỉ phục vụ đọc nội dung, không chứng minh đã kiểm tra bố cục Word.

| Nguồn | Đường dẫn | Vai trò |
| --- | --- | --- |
| SRS | `../Tài liệu đặc tả yêu cầu.docx` | FR-001–005, FR-036–038 và BR/NFR liên quan |
| Actor/UC | `../Tài liệu danh sách Actor, Module, Use Case và biểu đồ Use Case của hệ thống.docx` | UC-001–005/F01; UC-036–038/F10 |
| Phân tích F01 | `../tai_lieu_pha_phan_tich/F01 -.docx` | 5 nhóm, 18 use case con |
| Thiết kế F01 | `../tai_lieu_pha_thiet_ke/TK-F01-Quản_lý_mặt_bằng.docx` | 8 entity, service/port, API /api/v1/f01 |
| Phân tích F10 | `../tai_lieu_pha_phan_tich/F10 - Quản lý sự kiện và khuyến mãi.docx` | 9 use case, QR, tệp, voucher, công khai |
| Thiết kế F10 | `../tai_lieu_pha_thiet_ke/TK-F10-Quan_ly_su_kien_va_khuyen_mai.docx` | 12 lớp nghiệp vụ, 3 bảng liên kết, 9 API chi tiết |
| Mô hình miền | `../tai_lieu_pha_phan_tich/class_diagram_thuc_the_toan_he_thong_tieng_viet.puml` | Lớp, quan hệ, enum và hành vi |
| Quan hệ thực thể | `../tai_lieu_pha_phan_tich/Cac_lop_va_quan_he_thuc_the_17_module.docx` | Đối chiếu quan hệ/kiểu giá trị khi cần |
| SQL | `../schema.sql`, `../seed.sql` | Cấu trúc vật lý và dữ liệu mẫu |

Thiết kế F01 ghi rõ endpoint/DTO/chính sách lỗi là đề xuất. F10 có ví dụ chưa nhất quán với schema/mô hình. Bộ rule dùng thiết kế module làm baseline đề xuất, không coi đây là API đã được phê duyệt hoặc đang chạy.

## Mã use case

| Nhóm tổng thể | Nhóm chi tiết |
| --- | --- |
| UC-001 thông tin mặt bằng | F01UC01.1–01.5 |
| UC-002 cửa hàng/sạp | F01UC02.1–02.4 |
| UC-003 trạng thái | F01UC03.1–03.3 |
| UC-004 lịch sử | F01UC04.1–04.2 |
| UC-005 khai thác | F01UC05.1–05.4 |
| UC-036 sự kiện | F10UC01.1–01.5 |
| UC-037 khuyến mãi | F10UC02.1–02.3 |
| UC-038 tra cứu | F10UC03 |

## Điểm lệch cần xử lý

Ưu tiên chỉ dẫn mới của người dùng. SRS/phân tích xác định ý nghĩa nghiệp vụ; thiết kế đề xuất triển khai; SQL xác định cấu trúc vật lý hiện có. Trước khi code phần bị tác động, ghi lựa chọn, nguồn và ảnh hưởng trong code/docs/decisions/. Không âm thầm trộn phương án.

| Điểm lệch | Quy tắc |
| --- | --- |
| Enum tiếng Việt trong Word/seed, tiếng Anh trong PlantUML | Map riêng từng enum. Đề xuất mã nội bộ theo PlantUML, nhãn tiếng Việt; API tiếng Việt cần converter/contract test. DANG_THUE/DA_THUE/CHO_THUE chưa đủ nghĩa, không tự coi đồng nghĩa RESERVED/LEASED. |
| Word nói code cần unique, SQL đã có UNIQUE(code) | Kế thừa constraint thật. Availability phải đúng cả với mã lưu trữ; không chỉ lọc archived rồi báo khả dụng sai. |
| cua_hang.premise_id nullable, UC yêu cầu thuộc mặt bằng | Tạo mới bắt buộc premise hợp lệ; đọc dữ liệu cũ nullable an toàn. Chỉ thêm NOT NULL sau kiểm kê/chuyển dữ liệu. |
| F10 lớp dùng LocalDateTime/isDeleted/tên field khác SQL | Map rõ tên/cột; UTC và archivedAt theo schema, không tạo hai cờ xóa mâu thuẫn. |
| Ba bảng liên kết có UUID id riêng nhưng Word gợi ý ManyToMany | Dùng association entity/adapter tạo id đúng; ManyToMany đơn thuần không điền id NOT NULL thiếu default. |
| F10 annotation dùng /approve, /schedules; API chi tiết dùng /approval, /activities | Ưu tiên mục API chi tiết và bảng tổng hợp cuối Word; không mở hai alias tùy tiện. |
| Mô tả service lưu nháp, endpoint gửi đề xuất trả CHO_DUYET | Phân biệt lưu nháp và gửi duyệt. Proposals mang ý nghĩa gửi, đề xuất PENDING_APPROVAL; không bỏ bước chờ duyệt. |
| Role Word MANAGER/MKT_STAFF/ADMIN/ServiceStaff khác RoleCode chung | Map qua F16 theo quyền thật; không tạo role đồng nghĩa hoặc cho SYSTEM_ADMIN mọi quyền kinh doanh. |
| Redis DECR và MySQL là hai giao dịch | DB làm nguồn bền vững hoặc có reserve/compensate/reconcile; DECR không tự bảo đảm DB/Redis khớp. |
| Response ví dụ có redisStockKey, QR URL có token | Không lộ key nội bộ trong DTO công khai. URL/token QR nhạy cảm, chỉ cấp cho người có quyền, không log; ghi điều chỉnh contract. |
| UUID mẫu chứa s/u/v/p/q, ngày gửi sau ngày sự kiện | Dữ liệu minh họa sai; thay UUID/ngày test hợp lệ, không nới validation. |
| ROI có số mẫu nhưng thiếu nguồn doanh thu quy thuộc | Không dùng ngân sách/check-in làm doanh thu. Thiếu nguồn trả chưa xác định, không bịa ROI. |
| Service có check-in/apply/issue nhưng 9 API chưa đặc tả đủ | Bổ sung hợp đồng rõ cho thao tác hỗ trợ khi nhiệm vụ cần; không nhận đã có endpoint chuẩn. |

F01/F10 dùng MySQL và hợp đồng riêng theo thiết kế hai module. Không lấy backup/ảnh trong tmp/ thay nguồn gốc. Nếu thiếu quyết định kinh doanh không thể suy ra, hỏi đúng điểm đó và tiếp tục phần độc lập.
