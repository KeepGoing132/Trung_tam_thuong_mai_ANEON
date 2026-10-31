# Phạm vi nghiệp vụ và tích hợp F01 và F10

Chỉ F01 và F10 là phạm vi phát triển. Chi tiết nằm trong [F01](f01-quan-ly-mat-bang.md) và [F10](f10-su-kien-va-khuyen-mai.md). Không triển khai thiết bị/sự cố/bảo trì/bãi đỗ xe/tài chính hoặc toàn bộ cổng khách hàng từ tài liệu tổng thể.

| Module | Sở hữu trong phạm vi | Phụ thuộc |
| --- | --- | --- |
| F01 | Tòa nhà/tầng/khu vực/vị trí, mặt bằng, ngành hàng, cửa hàng, lịch sử theo 8 bảng | F02 khách thuê/liên kết; F03 hợp đồng; F16 quyền; F18 biểu diễn bản đồ |
| F10 | Sự kiện/hoạt động/chi phí, QR/lượt tham gia, khuyến mãi/voucher/lịch sử và liên kết cửa hàng | F01 vị trí/cửa hàng; F11 khách hàng/hóa đơn/điểm; F15 thông báo; F16 danh tính/quyền |

F01 cung cấp read port cho F10 về location/cửa hàng/trạng thái/publicVisible cần thiết. F10 chỉ lưu FK/liên kết tham gia, không sửa diện tích/trạng thái cửa hàng. Khi dữ liệu F01 thay đổi, xác định lại hợp lệ của lịch/chương trình theo hợp đồng, không cascade xóa nghiệp vụ đã phát sinh.

Thiết kế F01 có ContractQueryPort, OccupancyQueryPort, DigitalMapPort, AccessScopePort. F10 bổ sung port tối thiểu cho khách/hóa đơn/điểm/thông báo/doanh thu quy thuộc khi chức năng cần; tên port là đề xuất, không phải API ngoài đã có.

Port mô tả input/output/quyền/độ mới/timeout/lỗi/idempotency khi có side effect. Không gọi Controller module khác hoặc repository xuyên module để né hợp đồng.

Thiếu dữ liệu hợp đồng/diện tích khả dụng hoặc xác minh bản đồ bắt buộc: báo lỗi phụ thuộc, không đoán VACANT hoặc cho lưu mặc định. Đọc không phụ thuộc có thể tiếp tục. Lỗi hiển thị khác không thể xác minh vị trí; fallback chỉ khi quy tắc cho phép.

Adapter giả chỉ trong test/profile local có nhãn. Không báo tích hợp thật khi dùng giả. Bảng tham chiếu tối thiểu để chạy DB không nghĩa xây module phụ thuộc đầy đủ.

Thông báo duyệt/thưởng điểm/cache sau commit qua cơ chế có truy vết/retry khi cần. Không mất nghiệp vụ chính vì provider tạm lỗi, không trả đã gửi/đã cộng khi mới xếp hàng. Ghi rõ transaction giữa module cùng DB và dịch vụ ngoài.
