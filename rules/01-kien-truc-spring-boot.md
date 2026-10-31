# Kiến trúc Spring Boot cho F01 và F10

## Stack

Dùng Java Spring Boot, Spring MVC, Spring Data JPA/Hibernate, Jakarta Validation và Spring Security; MySQL 8.0 theo schema. Đề xuất Java 21, Maven với Maven Wrapper, Flyway và Spring Boot stable tương thích JDK/thư viện tại thời điểm khởi tạo. Pin phiên bản cụ thể; không SNAPSHOT/khoảng version hoặc tự nâng major khi sửa chức năng.

MapStruct được nhắc trong thiết kế F10, F01 cho phép mapper tương đương; nếu chọn phải cấu hình annotation processing đúng build. Redis/Spring Cache là lựa chọn thiết kế F10, không là nguồn duy nhất của tồn kho. MySQL Enterprise được nhắc trong Word nhưng SQL không cho thấy tính năng bắt buộc cần Enterprise; không tự yêu cầu giấy phép trả phí.

Một backend với f01, f10; không tự thêm microservice, API Gateway độc lập, frontend Angular/React hay app Flutter vì chúng được nhắc trong sơ đồ. Xác thực có thể thực hiện tại SecurityFilterChain của backend.

## Package theo thiết kế

```text
code/
  pom.xml
  mvnw, mvnw.cmd, .mvn/wrapper/
  src/main/java/com/company/aneon/
    AneonApplication.java
    f01/
      controller/      # MatBang, CuaHangSap, TrangThai, LichSu, KhaiThac
      service/         # impl/ khi theo thiết kế interface/implementation
      repository/
      entity/          # 8 entity theo schema
      dto/request/
      dto/response/
      mapper/
      integration/     # ContractQueryPort, OccupancyQueryPort, DigitalMapPort
      exception/
      constant/
    f10/
      controller/      # Event, Promotion, QRCode, Voucher, EventReport
      service/
      repository/
      entity/          # 12 lớp nghiệp vụ + association entity khi cần
      dto/request/
      dto/response/
      mapper/
      integration/     # Customer/Invoice/Loyalty/Notification port khi cần
      exception/
      constant/
    shared/            # security, error, audit và hạ tầng tối thiểu
  src/main/resources/application.yml
  src/main/resources/db/migration/
  src/test/java/com/company/aneon/
  docs/decisions/
```

Đây là cấu trúc đề xuất khi có nhiệm vụ code, chưa phải đã tồn tại. Giữ com.company.aneon.f01/f10 theo thiết kế nếu chưa có root package nhóm chốt; khi đã có convention thì kế thừa.

## Trách nhiệm

- Controller nhận path/query/header/DTO, validate cấu trúc, lấy danh tính, gọi service và trả response. Không gọi repository trực tiếp.
- Service sở hữu nghiệp vụ, quyền/scope, transaction, kiểm tra diện tích/trùng lịch/ngân sách/voucher và điều phối tích hợp. Tuân theo phân lớp F10, không buộc viết lại thành kiến trúc khác.
- Entity map đúng schema, giữ trạng thái/bất biến phù hợp; không gọi HTTP/Redis/provider. Hành vi miền đã có được giữ hoặc ủy nhiệm rõ tại service, không bỏ quy tắc vì bảng lớp F10 minh họa getter/setter.
- Repository truy vấn có scope/constraint/khóa, không trả ResponseEntity hoặc gửi thông báo.
- Mapper chỉ chuyển dữ liệu, không truy vấn DB/cấp quyền/chuyển trạng thái.
- Port/adapter giới hạn phụ thuộc ngoài. F10 đọc cửa hàng/vị trí qua F01, không sửa dữ liệu F01 qua repository riêng.

Constructor injection. Không BaseService CRUD bỏ qua nghiệp vụ hoặc package rỗng cho module ngoài phạm vi.

## Cấu hình

Tách local/test/production bằng profile và biến môi trường; secret không trong Git. Thời điểm lưu UTC, nghiệp vụ dùng zone cấu hình, đề xuất Asia/Ho_Chi_Minh; inject Clock. Tắt Open Session in View cho REST, map DTO trong service. Flyway quản lý schema, Hibernate validate ở môi trường tích hợp/production.

Cấu hình có validation cho datasource, JWT, Redis nếu dùng, timeout tích hợp, upload và giới hạn truy cập. Thiếu phụ thuộc bắt buộc báo lỗi rõ, không trả dữ liệu giả. Log có traceId/module/mã đối tượng, tránh dữ liệu nhạy cảm.

Nguồn kiểm tra tương thích khi bootstrap: [Spring Boot system requirements](https://docs.spring.io/spring-boot/system-requirements.html). Đọc tài liệu đúng phiên bản đã pin.
