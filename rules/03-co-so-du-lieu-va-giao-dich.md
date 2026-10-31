# Dữ liệu và giao dịch F01 và F10

## Mapping theo SQL hiện hữu

MySQL 8.0, InnoDB, utf8mb4, qldapm_aneon; bảng/cột lấy từ ../schema.sql. UUID CHAR(36) cần mapping Hibernate dạng ký tự. DATETIME(6) không có timezone nên cấu hình UTC và test round trip. Không copy PostgreSQL JSONB/partial index WHERE vào MySQL.

| Module | Entity/bảng |
| --- | --- |
| F01 – 8 bảng | ToaNha/toa_nha; Tang/tang; KhuVuc/khu_vuc; ViTri/vi_tri; MatBang/mat_bang; NganhHang/nganh_hang; CuaHangSap/cua_hang; LichSuMatBangVaCuaHang/lich_su_mat_bang_cua_hang |
| F10 sự kiện | SuKien/su_kien; HoatDongSuKien/hoat_dong_su_kien; ChiPhiThucTe/chi_phi_thuc_te; MaQrThamGia/ma_qr_tham_gia; LuotThamGiaSuKien/luot_tham_gia_su_kien |
| F10 khuyến mãi | ChuongTrinhKhuyenMai/chuong_trinh_khuyen_mai; ChuongTrinhGiamGia/chuong_trinh_giam_gia; ChuongTrinhTangPhieuUuDai/chuong_trinh_tang_phieu; ChuongTrinhTichDiem/chuong_trinh_tich_diem |
| F10 voucher/lịch sử | PhieuUuDai/phieu_uu_dai; PhieuUuDaiKhachHang/phieu_uu_dai_khach_hang; LanApDungKhuyenMai/lan_ap_dung_khuyen_mai |
| F10 ba bảng liên kết | su_kien_cua_hang; khuyen_mai_cua_hang; phieu_uu_dai_cua_hang |

F10 bảng cha/con chung PK: JPA JOINED hoặc mapping tương đương, không tạo SINGLE_TABLE. Ba bảng liên kết có UUID id riêng và UNIQUE cặp FK; dùng association entity tạo id đúng.

F01 diện tích DECIMAL(14,2), tọa độ DECIMAL(14,4); F10 tiền DECIMAL(18,2), tỷ lệ DECIMAL(9,4). Map đúng precision/scale/nullability/FK. person_in_charge của hoạt động là CHAR(36), tên người mẫu JSON không thể ghi thẳng vào cột: chọn DTO tham chiếu hoặc adapter lookup rõ.

Chỉ map audit/version khi bảng có cột. Lịch sử mặt bằng, lượt check-in, lần áp dụng khuyến mãi khác entity thay đổi được; không ép vào base entity có cột không tồn tại. archived_at bảo toàn lịch sử; không cascade xóa vật lý qua quan hệ khách hàng/hợp đồng/tài chính.

## Migration

Flyway migration bất biến V<version>__<mo_ta>.sql. DB rỗng test baseline từ đầu; DB đã có kiểm kê trước baseline, không baseline-on-migrate để giấu lệch schema. Không tùy tiện đưa CREATE DATABASE/USE/tắt FK vào production startup. Không ddl-auto update/create quản lý production.

Schema toàn hệ thống có FK đến module ngoài. Chạy riêng F01/F10 phải chọn: DB chung đã có cấu trúc phụ thuộc, hoặc baseline tối thiểu có các bảng tham chiếu thật sự cần cùng hợp đồng. Bảng tham chiếu để chạy DB không có nghĩa triển khai CRUD/module khác. Không xóa FK/tạo bảng giả chỉ để test pass.

Seed có enum Việt chưa khớp mô hình: fixture local/test phải kiểm chứng. Không nạp seed gốc làm bằng chứng nghiệp vụ đúng. Rà soát unique/index thiếu cho QR hiệu lực, check-in, issuance/application idempotency trước migration; có field idempotency_key chưa có nghĩa đã có UNIQUE.

## Transaction và đồng thời

- Transaction tại public service method qua Spring proxy, tránh self-invocation. Lỗi sau ghi rollback nghiệp vụ và lịch sử; không nuốt lỗi rồi commit.
- F01 khóa mặt bằng trước tính tổng area và thêm/sửa cửa hàng; chuyển giữa hai mặt bằng khóa cả hai theo ID cố định. Giảm totalArea dùng cùng quy ước khóa. Khả dụng theo thời gian phối hợp F02/F03 qua port.
- UNIQUE(code) chống request cạnh tranh, availability không thay constraint. SQL unique không loại archived nên mặc định chưa tái dùng code lưu trữ.
- F10 xếp lịch khóa vị trí chung giữa các event rồi kiểm tra/lưu; chỉ khóa event không chặn hai event đặt cùng sảnh. Nhiều vị trí khóa theo ID cố định, kiểm tra cả trùng trong batch.
- @Version cho bảng hỗ trợ; expectedVersion qua DTO/header theo quyết định API. Không gán version client trực tiếp vào @Version. Xung đột 409 theo baseline hai module.
- Scope trong items/COUNT/summary, sort ổn định. Projection/fetch phù hợp, tránh N+1 và collection fetch join làm sai phân trang.

## Voucher, Redis và side effect

Đề xuất MySQL transaction là nguồn bền vững cho issued_count/used_count, quyền sở hữu và idempotency. Kiểm tra/tăng bộ đếm có điều kiện hoặc khóa voucher. Giới hạn theo khách cần khóa/constraint/counter phù hợp để hai request không vượt hạn mức. issuedLimit=0 nghĩa không giới hạn, không phải hết kho.

Redis DECR atomic không gộp được transaction MySQL. Redis làm cache thì cập nhật/invalidate sau commit và khôi phục từ DB. Nếu reserve trước DB phải có reservation ID/timeout/compensation/reconciliation; test DB rollback, Redis timeout, crash giữa hai bước. Không hoàn kho mù khi chưa biết reserve thành công.

Idempotency bền vững cho phát/redeem/apply/thưởng khi triển khai các thao tác đó: scope actor/operation/key, hash và kết quả. Cùng key khác payload là xung đột. Không chỉ exists rồi insert thiếu constraint.

Thông báo duyệt/tích điểm ngoài chạy sau commit qua outbox/adapter có retry khi cần; không giữ khóa DB lúc gọi provider. Check-in lặp không cộng điểm hai lần. Bảng hạ tầng thêm phải có migration giải thích phục vụ F01/F10.
