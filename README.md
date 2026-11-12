# TTTM ANEON — Backend F01 + F10 (Java Spring Boot)

Backend cho **F01 – Quản lý mặt bằng** và **F10 – Quản lý sự kiện và khuyến mãi** của hệ
thống quản lý Trung tâm Thương mại Thông minh ANEON. Bộ rule nghiệp vụ nằm trong
[DEEPSEEK.md](DEEPSEEK.md) và `rules/`; tài liệu phân tích/thiết kế gốc nằm ở thư mục cha
của repository (`../tai_lieu_pha_phan_tich`, `../tai_lieu_pha_thiet_ke`, `../schema.sql`).

## Stack

- Java 21 (biên dịch `--release 21`, chạy được trên JDK 25), Spring Boot 3.5.7, Maven Wrapper 3.9.11
- Spring MVC + Spring Data JPA/Hibernate + Spring Security, Jakarta Validation
- MySQL 8.0 (schema `qldapm_aneon`), Flyway migration, Hibernate `ddl-auto: validate`
- Test: JUnit 5, Mockito, MockMvc, Testcontainers MySQL (`*IT` chạy qua Failsafe)

## Cấu trúc

```text
src/main/java/com/company/aneon/
  AneonApplication.java
  shared/            # envelope ApiResponse, lỗi chuẩn, security/scope, entity audit, reference
  f01/               # Quản lý mặt bằng: 8 entity, 5 service, 5 controller, 18 UC
  f10/               # Sự kiện & khuyến mãi: 15 bảng, 6 service, controller, 9 UC + bổ sung
src/main/resources/db/migration/   # V1 baseline, V2 ràng buộc, V3 bảng tham chiếu, V4 nội dung tệp
src/test/java/com/company/aneon/   # unit test + IT (Testcontainers hoặc MySQL ngoài)
docs/decisions/      # Quyết định thiết kế (D0001–D0010)
docs/openapi/        # Hợp đồng OpenAPI F01/F10
```

## Chạy local (MySQL trên máy)

1. MySQL 8.0 đang chạy (service `MySQL80`, cổng 3306). Tạo database và user:

```sql
CREATE DATABASE qldapm_aneon CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'aneon'@'localhost' IDENTIFIED BY 'aneon';
GRANT ALL PRIVILEGES ON qldapm_aneon.* TO 'aneon'@'localhost';
```

2. Chạy app để Flyway tự áp dụng các migration:

```powershell
.\mvnw.cmd spring-boot:run
# hoặc đặt biến môi trường:
#   ANEON_DB_URL=jdbc:mysql://127.0.0.1:3306/qldapm_aneon?connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true
#   ANEON_DB_USERNAME=aneon   ANEON_DB_PASSWORD=aneon
```

3. Profile `local` bật danh tính stub qua header (chưa có identity provider — xem
   `docs/decisions/0008`):

```
X-Actor-Id: <UUID tai khoan ton tai trong tai_khoan_nguoi_dung>
X-Actor-Roles: MANAGEMENT            # DIRECTOR/MANAGEMENT/OPERATIONS/ACCOUNTING/MARKETING/CUSTOMER...
X-Actor-Zones: <zoneId1,zoneId2>     # tuy chon: gioi han pham vi du lieu
```

Ví dụ:

```powershell
curl -X POST http://localhost:8080/api/v1/f01/premises `
  -H "Content-Type: application/json" `
  -H "X-Actor-Id: 11111111-1111-1111-1111-111111111111" -H "X-Actor-Roles: MANAGEMENT" `
  -d '{"code":"MB001","name":"Mặt bằng A1","zoneId":"...","totalArea":120,"status":"VACANT"}'
```

`GET /api/v1/f10/promotions-events/search` công khai, không cần header.

## Kiểm thử

```powershell
.\mvnw.cmd test      # Unit test (Mockito, không cần DB)
.\mvnw.cmd verify    # Kiểm thử tích hợp trên MySQL thật
```

IT (`F01WebFlowIT`, `F10ConcurrencyIT`) mặc định dùng **Testcontainers MySQL 8.0**
(cần Docker). Nếu Docker không khả dụng, trỏ tới một MySQL 8.0 thật bằng biến môi
trường trước khi chạy `verify`:

```powershell
$env:ANEON_IT_MYSQL_URL  = "jdbc:mysql://127.0.0.1:3306/qldapm_aneon_it?connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true&allowPublicKeyRetrieval=true&useSSL=false"
$env:ANEON_IT_MYSQL_USER = "aneon"
$env:ANEON_IT_MYSQL_PASSWORD = "aneon"
.\mvnw.cmd verify
```

Database test (`qldapm_aneon_it`) được Flyway khởi tạo và dữ liệu test tích lũy giữa
các lần chạy — các mã dùng trong IT sinh duy nhất theo thời gian nên không xung đột.

## Trạng thái triển khai

- **F01**: đủ 18 use case con (tra cứu/thêm/sửa mặt bằng, kiểm tra mã, vị trí bản đồ,
  cửa hàng/sạp + diện tích, trạng thái + đồng bộ hợp đồng F03, lịch sử, khai thác + 3 bộ lọc).
- **F10**: đủ 9 use case (đề xuất → duyệt/từ chối, xếp lịch chống trùng sảnh, QR check-in,
  báo cáo, khuyến mãi 3 loại, kho voucher, realtime, public search) + endpoint bổ sung
  có đánh dấu trong `docs/decisions/0004` (chi tiết sự kiện, ghi chi phí, check-in,
  phát/dùng voucher, áp dụng khuyến mãi).
- Các quyết định lệch tài liệu được ghi tại `docs/decisions/` (enum D0001, baseline CSDL
  D0002, hợp đồng API D0003/D0004, ma trận trạng thái D0005, không dùng Redis D0006,
  QR/check-in D0007, danh tính D0008, vòng đời F10 D0009, dữ liệu tham chiếu và luồng bổ sung D0010).

## Chưa triển khai (ngoài phạm vi giao)

- Nhà cung cấp danh tính và ma trận quyền F16 chính thức, ứng dụng quản lý độc lập của
  F02/F03/F11/F18, thông báo F15, doanh thu quy thuộc để tính ROI và Redis/cache.
- F01 đọc hợp đồng, khách thuê và sơ đồ đã phát hành từ MySQL. F10 đối chiếu hóa đơn,
  gian hàng, hạng thành viên và lưu nội dung tệp đề xuất trong MySQL.

---

## Frontend — AEON MALL và quản lý ANEON (`frontend/`)

| Đường dẫn | Chức năng |
| --- | --- |
| `/` | Trang giới thiệu, gian hàng mẫu có bộ lọc, sự kiện và ưu đãi lấy từ F10 |
| `/kham-pha` | Khách tra cứu sự kiện và ưu đãi theo tên, danh mục và trang |
| `/check-in` | Khách xác nhận tham gia từ liên kết có `event` và `token` do F10 tạo |
| `/quan-ly` | Nhân viên quản lý mặt bằng, cửa hàng, trạng thái, lịch sử, khai thác (F01), sự kiện, khuyến mãi và voucher (F10) |

Chạy Spring Boot theo phần trên. Để thử nghiệp vụ bằng danh tính local, mở PowerShell ở thư mục `code`:

```powershell
$env:ANEON_PUBLIC_CHECKIN_BASE_URL = 'http://localhost:3000/check-in'
.\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=local'
```

Trong cửa sổ PowerShell khác:

```powershell
cd frontend
npm install
$env:NEXT_PUBLIC_ANEON_LOCAL_IDENTITY = 'true' # chỉ dùng khi backend chạy profile local
npm run dev      # http://localhost:3000
```

- Next.js chuyển tiếp `/aneon-api/*` tới Spring Boot ở `http://127.0.0.1:8080/api/v1/*`.
  Đặt `ANEON_API_ORIGIN` trước khi chạy Next.js nếu backend dùng địa chỉ khác.
- Ở `/quan-ly`, nhập UUID trong `tai_khoan_nguoi_dung` và chọn vai trò local. Tạo hoặc sửa
  bản ghi cần tài khoản và các UUID tham chiếu đã có trong MySQL. Ví dụ: tạo mặt bằng cần
  `khu_vuc.id`, đề xuất sự kiện cần `vi_tri.id`, và check-in của khách cần tài khoản có
  `customer_id` trỏ tới `khach_mua_sam.id`.
- Khi đã có JWT từ hệ thống định danh, giao diện gửi `Authorization: Bearer …`. Tắt
  chế độ local ở cả hai ứng dụng khi triển khai; dự án hiện chưa tích hợp nhà cung cấp định danh.
- Dùng `npm run build` và `npm start` để chạy bản production.
- Nạp dữ liệu demo sau khi Flyway chạy xong bằng lệnh `SOURCE E:/duong-dan-den-code/scripts/seed-local.sql;`
  trong MySQL client đã chọn database demo. Script bổ sung bản ghi mẫu còn thiếu và giữ bản ghi đã tồn tại.
  Tài khoản Ban quản lý là `11111111-1111-1111-1111-111111111111`, Marketing là
  `22222222-2222-2222-2222-222222222222`, khách là `33333333-3333-3333-3333-333333333333`.
  Hợp đồng mẫu có UUID `a0000000-0000-4000-8000-000000000007` và hóa đơn mẫu có UUID
  `a0000000-0000-4000-8000-000000000020`.
- **Ảnh**: Unsplash (giấy phép Unsplash, đã kiểm tra nội dung từng ảnh). Prompt tạo bộ
  ảnh AI đồng bộ nằm ở `frontend/docs/image-prompts.md`.
- **Kiểm tra**: `node scripts/screenshot.mjs` chụp desktop/mobile bằng Edge headless
  (puppeteer-core); `node scripts/interaction-test.mjs` tự động kiểm tra tìm kiếm/lọc.
- Hỗ trợ `prefers-reduced-motion` (tắt zoom/marquee/gradient và reveal).
