# D0010 – Dữ liệu tham chiếu và luồng bổ sung F01/F10

- V3 lấy sáu bảng tham chiếu từ `schema.sql` để đọc hợp đồng, khách thuê, sơ đồ tầng và hạng thành viên trên cùng MySQL. Các nghiệp vụ nhập liệu của module sở hữu tiếp tục thuộc F02, F03, F11 và F18.
- F01 chỉ cho gắn vị trí có tọa độ nằm trong biên của sơ đồ tầng đã phát hành. API trả các điểm của khu vực mặt bằng sau khi kiểm tra phạm vi. Danh sách khách thuê lấy từ liên kết khai thác còn hiệu lực và chỉ trả mã, tên và UUID.
- F10 lưu, sửa và gửi bản nháp theo tài khoản lập đề xuất. Hoạt động có danh sách, cập nhật theo version và thao tác gỡ bằng `archived_at`. Cập nhật lịch giữ kiểm tra phạm vi, thời gian và giao khoảng tại cùng vị trí.
- V4 tách nội dung tệp khỏi metadata. Tệp được gửi theo từng yêu cầu multipart, gắn với bản nháp và tải xuống qua API nội bộ có kiểm tra quyền. Nội dung lưu trong MySQL cùng giao dịch với metadata.
- Áp dụng khuyến mãi kiểm tra gian hàng ghi trên hóa đơn và hạng của tài khoản thành viên đang hoạt động. Phiếu áp dụng cho chương trình tặng voucher phải thuộc đúng mẫu và còn hiệu lực.
- Dữ liệu trong `scripts/seed-local.sql` phục vụ demo có thể nạp lại, gồm quan hệ thuê, sơ đồ, thành viên, hóa đơn, voucher và chi phí sự kiện. Thời gian mẫu được tính tại lần nạp đầu tiên.
