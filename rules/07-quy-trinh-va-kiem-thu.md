# Quy trình triển khai và kiểm thử F01 và F10

## Trước khi sửa

1. Đọc yêu cầu mới nhất, DEEPSEEK.md, rule 00/01/06 và rule module. Kiểm git status/build/config/file định sửa, giữ thay đổi ngoài nhiệm vụ.
2. Xác định UC con, actor, tiền điều kiện, thành công/thay thế/lỗi/hậu điều kiện. Đối chiếu Word với SQL/PlantUML, ghi quyết định cho điểm lệch ảnh hưởng.
3. Chỉ bootstrap khi người dùng giao code. Pin phiên bản tương thích, chọn baseline DB/phụ thuộc tối thiểu/cấu hình test. Yêu cầu hiện tại chỉ là rule, không tạo ứng dụng.
4. Nêu kế hoạch/cách kiểm chứng ngắn. Hỏi khi thiếu quyết định kinh doanh không thể suy ra, tiếp tục phần độc lập; không xin lại xác nhận cho lựa chọn kỹ thuật thường trong phạm vi giao.

## Thực hiện

Theo UC: contract → migration/mapping cần thiết → repository có scope/khóa → service/transaction → DTO/mapper/controller → security → test. Đổi thứ tự theo phụ thuộc nhưng không bỏ phần cần.

Không CRUD module ngoài F01/F10. Port/adapter giả local/mock ghi nhãn, không báo tích hợp thật đã xong. Không format/rename toàn repo, nâng dependency hoặc mở endpoint không liên quan. Không chạy schema/seed trên dữ liệu thật để thử. Đổi contract cập nhật tài liệu/test cùng phạm vi.

## Kiểm thử theo rủi ro

| Loại | Kiểm chứng |
| --- | --- |
| Java/service | Bất biến, validation chéo, trạng thái, tỷ lệ/đơn vị tiền, ngày giờ |
| MVC/contract | Path/envelope code-message-data/required-null/enum-UUID/version/lỗi |
| Security | 401/403/scope list-detail-count-summary-history-report-file; public không lộ nội bộ |
| JPA/migration | MySQL cùng major/config tương ứng; UUID CHAR(36), UTC DATETIME, JSON, JOINED, association id |
| Transaction | Lỗi sau ghi rollback nghiệp vụ/lịch sử/counter; batch không ghi một phần |
| Đồng thời | Hai transaction thật: code/area/sảnh/duyệt/QR active/check-in/voucher cuối/per-customer |
| Adapter/cache | F03/F18 lỗi/provider timeout; Redis reserve và DB rollback, crash, cache phục hồi/reconcile |

Bộ test tương thích Spring Boot: JUnit Jupiter, Mockito, Spring Security Test, Testcontainers MySQL. Không dùng mock/H2 để kết luận transaction/constraint/khóa hàng/JSON MySQL đúng. Markdown chỉ kiểm nội dung/encoding/liên kết, không tạo test Java hình thức.

## Nghiệm thu module

F01: đủ 18 UC; code trùng tạo/sửa; area bằng/ngang/vượt; giảm mặt bằng/chuyển cửa hàng; zone-location sai; hợp đồng hết còn liên kết; version cũ; lịch sử rollback/bất biến; lọc tầng-khu-status và summary toàn scope.

F10: đủ 9 UC; thiếu dữ liệu/file sai; pending không public; duyệt cạnh tranh; lịch ngoài/trùng sảnh/batch; một QR active; QR cũ/ngoài giờ bị chặn; check-in không thưởng đôi; voucher unlimited=0/hạn mức cuối/per-customer/redeem lặp; báo cáo không nhân số; ROI thiếu doanh thu; public pagination đúng.

Chọn kiểm tra đủ chứng minh thay đổi/phụ thuộc, không bắt buộc chạy mọi chức năng cho sửa nhỏ.

## Lệnh và bàn giao

Khi đã có Maven Wrapper, từ code/ trên Windows:

```powershell
.\mvnw.cmd test
.\mvnw.cmd verify
```

Chọn lệnh đủ bao phủ, không phải chạy lặp cả hai. Test *IT phải cấu hình Failsafe để verify thực sự chạy. Testcontainers cần Docker; thiếu JDK/Docker/network/config ghi rõ, không nói mọi test pass. skipTests không là chứng cứ kiểm thử.

Hoàn tất khi đúng contract/UC, scope đầy đủ, migration/transaction/constraint phù hợp, không stub trên luồng báo xong và check liên quan đã chạy hoặc giới hạn được nêu. Báo thay đổi/lý do/file chính/lệnh-kết quả thực tế/phần thiếu. Kiểm tài liệu không chứng minh backend hoạt động.
