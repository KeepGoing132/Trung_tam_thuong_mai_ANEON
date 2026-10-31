# Hướng dẫn DeepSeek phát triển F01 và F10 của ANEON

Bạn là lập trình viên Java Spring Boot của ANEON. **Chỉ triển khai F01 (F1) – Quản lý mặt bằng và F10 – Quản lý sự kiện và khuyến mãi.** Đọc rule và nguồn liên quan trước khi viết mã; thực hiện đúng chức năng người dùng giao trong hai module.

## Cách sử dụng

Markdown không bảo đảm được DeepSeek hoặc IDE tự nạp. Với DeepSeek Chat, đính kèm file này, các rule liên quan và tài liệu nguồn. Với công cụ coding dùng DeepSeek, cấu hình đưa các file vào ngữ cảnh theo cơ chế của công cụ. Không coi file có trên đĩa là đã được đọc.

Các đường dẫn nguồn được ghi từ thư mục `code/`. Tài liệu gốc hiện nằm ngoài repository code, ở thư mục cha. Khi chỉ clone repository này, cần cung cấp thêm nguồn trong rule 00. Nếu thiếu file, nêu đúng nguồn thiếu; không bịa nội dung đã đọc.

## Danh mục rule

| File | Nội dung |
| --- | --- |
| [00 Bối cảnh và nguồn](rules/00-boi-canh-va-nguon.md) | Phạm vi, nguồn, mâu thuẫn tài liệu |
| [01 Kiến trúc Spring Boot](rules/01-kien-truc-spring-boot.md) | Stack, package F01/F10, trách nhiệm các lớp |
| [02 Quy tắc Java](rules/02-quy-tac-java.md) | Tên, kiểu dữ liệu, injection, ngoại lệ |
| [03 Dữ liệu và giao dịch](rules/03-co-so-du-lieu-va-giao-dich.md) | Mapping, migration, khóa, voucher, Redis |
| [04 REST API](rules/04-rest-api-va-validation.md) | Path, DTO, HTTP và validation |
| [05 Bảo mật](rules/05-bao-mat-va-phan-quyen.md) | Quyền, scope, tệp và dữ liệu công khai |
| [06 Phạm vi và tích hợp](rules/06-nghiep-vu-aneon.md) | Ranh giới hai module với các phụ thuộc |
| [F01 Quản lý mặt bằng](rules/f01-quan-ly-mat-bang.md) | 18 use case con F01 |
| [F10 Sự kiện và khuyến mãi](rules/f10-su-kien-va-khuyen-mai.md) | 9 use case F10 |
| [07 Quy trình và kiểm thử](rules/07-quy-trinh-va-kiem-thu.md) | Thực hiện, kiểm chứng và bàn giao |

Lần khởi tạo đọc toàn bộ. Các lần sau tối thiểu đọc 00, 01, 06, 07, rule module liên quan và rule kỹ thuật bị tác động.

## Quy tắc bắt buộc

1. Trước khi sửa, nêu ngắn gọn module/use case, nguồn đã đọc, file dự định sửa và cách kiểm chứng. Phân biệt yêu cầu nguồn với lựa chọn triển khai đề xuất.
2. Chỉ tạo module nghiệp vụ f01, f10 cùng hạ tầng dùng chung cần thiết. F02/F03/F11/F15/F16/F18 là phụ thuộc qua port/adapter, không là hạng mục triển khai đầy đủ.
3. Endpoint, lỗi, giới hạn tệp, version và idempotency phải theo thiết kế F01/F10 hoặc quyết định bổ sung đã ghi rõ.
4. Không bịa enum, ma trận trạng thái, số liệu ROI, nguồn doanh thu, quyền hoặc kết quả test. Đọc điểm lệch trong rule 00 trước khi chọn hợp đồng.
5. Hoàn thành từng use case có thể kiểm chứng: migration cần thiết, service/transaction, API, scope và test tương ứng. CRUD khung chưa phải nghiệp vụ hoàn tất.
6. Mọi file rule .md của bộ này nằm trong code/. Không sửa tài liệu nguồn ngoài code/ khi nhiệm vụ chỉ yêu cầu lập rule hoặc viết backend.
7. Yêu cầu hiện tại chỉ là bộ rule; không tự tạo ứng dụng, frontend hoặc triển khai dịch vụ. Khi người dùng giao code tiếp theo mới làm phần tương ứng.

## Prompt dùng ngay

```text
Đọc code/DEEPSEEK.md, các rule liên quan trong code/rules/ và tài liệu nguồn F01/F10.
Phạm vi tôi thực hiện chỉ gồm F01 và F10, công nghệ Java Spring Boot + MySQL.
Nhiệm vụ lần này: [module, mã use case và chức năng cần triển khai].
Nêu ngắn gọn nguồn đã đọc và điểm chưa thống nhất có ảnh hưởng, rồi triển khai trong phạm vi nhiệm vụ.
Module khác chỉ tích hợp qua port/adapter; không tự xây dựng lại.
Kiểm chứng thay đổi và báo kết quả thực tế, phần chưa chạy hoặc còn thiếu.
```

Nếu IDE mở trực tiếp code/, dùng đường dẫn DEEPSEEK.md và rules/ trong prompt.
