# Rule nghiệp vụ F01 quản lý mặt bằng

Nguồn: phân tích F01, TK-F01, SRS FR-001–005/BR-001–003, mô hình miền và schema. F1 trong yêu cầu người dùng là F01.

## Đủ 18 use case con

| Mã | Hành vi |
| --- | --- |
| F01UC01.1 | Danh sách/chi tiết mặt bằng, đúng scope |
| F01UC01.2 | Thêm, kiểm tra code/diện tích/tham chiếu, ghi lịch sử |
| F01UC01.3 | Sửa, giữ unique, kiểm soát version/diện tích đã phân bổ |
| F01UC01.4 | Code khả dụng, excludeId khi sửa |
| F01UC01.5 | Cập nhật vị trí hợp lệ trên bản đồ, ghi lịch sử |
| F01UC02.1 | Danh sách/chi tiết cửa hàng/sạp và liên quan khách thuê theo quyền |
| F01UC02.2 | Thêm cửa hàng trong mặt bằng còn diện tích phù hợp |
| F01UC02.3 | Sửa cửa hàng, code/liên kết/diện tích hợp lệ |
| F01UC02.4 | Chuyển trạng thái cửa hàng theo quy tắc được xác định |
| F01UC03.1 | Xem trạng thái hiện hành mặt bằng |
| F01UC03.2 | Cập nhật trạng thái, kiểm tra hợp đồng, ghi lịch sử |
| F01UC03.3 | Đồng bộ trạng thái theo hợp đồng hiện hành qua F03 |
| F01UC04.1 | Xem lịch sử mặt bằng |
| F01UC04.2 | Tra cứu lịch sử theo đối tượng/thời gian/nội dung/người thao tác |
| F01UC05.1 | Tổng quan/chi tiết khai thác từ trạng thái hiện hành |
| F01UC05.2 | Lọc theo khu vực đang hoạt động |
| F01UC05.3 | Lọc theo tầng thuộc danh mục TTTM |
| F01UC05.4 | Lọc theo trạng thái hợp lệ |

Các UC lọc dùng chung utilization query, không cần bảng tổng hợp mới. 8 entity: ToaNha, Tang, KhuVuc, ViTri, MatBang, NganhHang, CuaHangSap, LichSuMatBangVaCuaHang.

## Mã, diện tích và liên kết

Mã mặt bằng/cửa hàng unique theo SQL. Availability không thay kiểm tra lúc commit; sửa loại đúng bản ghi hiện tại. Chuẩn hóa hoa/thường/trim phải thống nhất collation, không nhận hai code khác nhau trái so sánh DB.

totalArea > 0; cửa hàng mới area > 0 và premise hợp lệ. Tổng diện tích các cửa hàng được tính vào phân bổ không vượt totalArea. Khi sửa cửa hàng loại bản cũ khỏi tổng rồi thêm giá trị mới, không cộng đôi.

Thu hẹp mặt bằng/chuyển cửa hàng sang mặt bằng khác kiểm tra lại phần đã phân bổ, FK, vị trí, quyền và hợp đồng liên quan; không chỉ kiểm lúc tạo. Phân biệt diện tích vật lý cửa hàng và diện tích thuê khả dụng theo khoảng ngày. availableArea(range)/canLease(range,area) cần F02/F03; không trừ mọi hợp đồng mọi thời điểm hoặc bỏ ngày hiệu lực.

Khóa mặt bằng và kiểm tra trong transaction để hai request vượt diện tích không thể cùng commit. Dữ liệu lịch sử premiseId nullable được đọc an toàn, không làm chuẩn cho tạo mới.

## Vị trí, trạng thái và hợp đồng

ViTri thuộc KhuVuc, KhuVuc thuộc Tang, Tang thuộc ToaNha. zoneId mặt bằng và location.zone nhất quán. Vị trí bản đồ hợp lệ, không ngoài sơ đồ; một mặt bằng một vị trí hiện hành. F18 tích hợp hiển thị/kiểm tra, không thay location_id F01.

PlantUML PremiseStatus/StoreStatus: VACANT, RESERVED, LEASED, MAINTENANCE, SUSPENDED, RETIRED. Word F01 tập tiếng Việt khác. Trước transition ghi mapping/ý nghĩa, đặc biệt DANG_THUE/DA_THUE/CHO_THUE chưa đủ rõ. Không cho mọi trạng thái chuyển với nhau hoặc dịch một-một thiếu căn cứ.

Hợp đồng hiệu lực phải phản ánh đúng trạng thái khai thác. Không đổi tay thành trống trái hợp đồng. Đồng bộ kiểm hợp đồng thuộc đúng mặt bằng/đúng thời gian, không tin contractId client. Hợp đồng kết thúc chưa đủ đặt VACANT nếu còn hợp đồng/liên kết khác hoặc đang bảo trì/ngừng khai thác. F03 lỗi không giả coi không có hợp đồng.

## Lịch sử và tra cứu

Lưu thay đổi cùng transaction với lịch sử: target_type/target_id/action/old_value_json/new_value_json/changed_by/reason và audit theo schema. Actor từ danh tính xác thực. Lịch sử chỉ thêm, không API sửa/xóa cho người dùng thường. Target đa hình thiếu FK trực tiếp thì service kiểm loại/ID.

Scope cho list/detail/history/summary khai thác. Bộ lọc tầng/khu vực/status kết hợp nhất quán; tầng và khu vực mâu thuẫn không trả dữ liệu sai. Summary trên toàn bộ tập đã lọc/scope, không chỉ trang hiện tại. Không kết quả là danh sách rỗng/tổng 0; lỗi phụ thuộc là lỗi riêng, không giả 0.

## Kiểm chứng

Code trùng tạo/sửa/cạnh tranh; area bằng/nhỏ/lớn hơn phần còn lại; giảm totalArea; đổi premise; sai zone/location; version cũ; rollback khi ghi history lỗi; scope list/count/summary/history; hợp đồng kết thúc còn liên kết; F03/F18 không sẵn sàng.

Không tự thêm quản lý hợp đồng, thu tiền, trình biên tập bản đồ đầy đủ hoặc DELETE mặt bằng/cửa hàng khi chưa có nhiệm vụ.
