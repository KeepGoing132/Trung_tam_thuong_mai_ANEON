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
src/main/resources/db/migration/   # V1 baseline (28 bảng) + V2 ràng buộc bổ sung
src/test/java/com/company/aneon/   # unit test + IT (Testcontainers hoặc MySQL ngoài)
docs/decisions/      # 9 quyết định thiết kế (D0001–D0009)
docs/openapi/        # Hợp đồng OpenAPI F01/F10
```

## Chạy local (MySQL trên máy)

1. MySQL 8.0 đang chạy (service `MySQL80`, cổng 3306). Tạo database và user:

```sql
CREATE DATABASE qldapm_aneon CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'aneon'@'localhost' IDENTIFIED BY 'aneon';
GRANT ALL PRIVILEGES ON qldapm_aneon.* TO 'aneon'@'localhost';
```

2. Chạy app (Flyway tự tạo 28 bảng F01/F10 + bảng dùng chung lần đầu):

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
.\mvnw.cmd test      # 60 unit test (Mockito, không cần DB)
.\mvnw.cmd verify    # + 16 IT trên MySQL thật
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
  QR/check-in D0007, danh tính D0008, vòng đời F10 D0009).

## Chưa triển khai (ngoài phạm vi giao)

- JWT thật + ma trận quyền F16 (hiện dùng mapping đề xuất trong D0008), upload tệp thật
  (chỉ kiểm tra metadata), tích hợp F02/F03/F11/F15/F18 thật (chạy qua port + stub local
  có nhãn), Redis/cache, thông báo F15 thật.

---

## Frontend — Website AEON MALL (`frontend/`)

Website công khai của trung tâm thương mại (tương ứng ý tưởng cổng F12), dựng theo tinh
thần thiết kế của godaylight.com (nền kem, display serif lớn, gradient động, editorial)
nhưng dùng màu nhận diện AEON (magenta trên nền cream).

```powershell
cd frontend
npm install
npm run dev      # http://localhost:3000
npm run build    # production build
npm start        # chạy bản production
```

- **Stack**: Next.js 15 (App Router) + Tailwind CSS v4 + Framer Motion; TypeScript.
- **Sections**: Header sticky chuyển trạng thái, Hero fullscreen (zoom chậm + staggered
  headline), Marquee, About, Store Directory (tìm kiếm + lọc danh mục/tầng hoạt động thật),
  Mua sắm & Ẩm thực, Ưu đãi (thẻ khung gradient động), Sự kiện (lọc theo tag), Tiện ích,
  Footer (đăng ký bản tin có trạng thái thành công).
- **Ảnh**: Unsplash (giấy phép Unsplash, đã kiểm tra nội dung từng ảnh). Prompt tạo bộ
  ảnh AI đồng bộ nằm ở `frontend/docs/image-prompts.md`.
- **Kiểm tra**: `node scripts/screenshot.mjs` chụp desktop/mobile bằng Edge headless
  (puppeteer-core); `node scripts/interaction-test.mjs` tự động kiểm tra tìm kiếm/lọc.
- Hỗ trợ `prefers-reduced-motion` (tắt zoom/marquee/gradient và reveal).
