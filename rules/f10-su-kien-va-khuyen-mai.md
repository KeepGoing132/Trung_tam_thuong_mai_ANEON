# Rule nghiệp vụ F10 sự kiện và khuyến mãi

Nguồn: phân tích F10, TK-F10, SRS FR-036–038, mô hình và 15 bảng F10. Ngân sách/ngày/UUID/số lượng mẫu trong Word không phải cấu hình cố định.

## Đủ 9 use case

| Mã | Nội dung |
| --- | --- |
| F10UC01.1 | Marketing đề xuất sự kiện/dự toán/cửa hàng tham gia/file kế hoạch, gửi duyệt |
| F10UC01.2 | Hoạt động nằm trong thời gian sự kiện, chống trùng vị trí/khung giờ |
| F10UC01.3 | QR cho sự kiện đã duyệt, duy nhất QR hiệu lực, giới hạn thời gian check-in |
| F10UC01.4 | Thông tin, tổng chi phí, lượt tham gia, báo cáo dữ liệu thật |
| F10UC01.5 | Quản lý duyệt/từ chối, ngân sách chính thức, lý do, phản hồi Marketing |
| F10UC02.1 | Đúng một loại giảm giá/tặng voucher/tích điểm và điều kiện |
| F10UC02.2 | Kho quà/voucher: code/mức giảm/số lượng/hạn dùng/giới hạn khách/cửa hàng |
| F10UC02.3 | Theo dõi phát/dùng/còn lại, số liệu cập nhật có nguồn |
| F10UC03 | Tìm kiếm sự kiện/khuyến mãi công khai, lọc hợp lệ |

## Đề xuất và phê duyệt

Tên/địa điểm/thời gian/ngân sách dự kiến bắt buộc khi gửi. Ngân sách không âm, startAt < endAt, vị trí/cửa hàng hợp lệ. File PDF/DOCX/PNG/JPG <=25 MB/file, key có quyền sở hữu.

Mô hình có DRAFT, PENDING_APPROVAL, APPROVED, REJECTED, ONGOING, FINISHED, CANCELLED. Tập trạng thái không có nghĩa được chuyển tùy ý. Baseline đề xuất: lưu nháp nếu có chức năng riêng là DRAFT; gửi đề xuất PENDING_APPROVAL; chỉ pending được duyệt/từ chối. Approve ghi ngân sách/người/thời điểm, reject bắt buộc lý do. Chạy/kết thúc/hủy/sửa sau duyệt cần đặc tả trước khi mở API, không suy diễn toàn bộ ma trận.

Client không gán approvedBy/approvedBudget trong tạo đề xuất. Hai quản lý duyệt/từ chối cạnh tranh chỉ một kết quả commit qua version/khóa. Transaction lỗi giữ trạng thái cũ. Thông báo sau commit, trạng thái gửi đúng thực tế.

## Lịch hoạt động

Khoảng hoạt động hợp lệ trong event; personInCharge tham chiếu đã xác minh, không ghi tên vào UUID. Kiểm tra trùng trong batch và với event khác cùng location.

Đề xuất [start,end), trùng khi existingStart < newEnd && existingEnd > newStart, lịch tiếp giáp không trùng. Word dùng ký hiệu khoảng đóng nên phải ghi quyết định ranh giới, không coi nửa mở đã chốt. Xác định trạng thái nào chiếm sảnh; không tự cho mọi draft/hủy giữ chỗ.

Khóa vị trí chung khi kiểm/lưu, không chỉ event. Batch lỗi rollback toàn batch. Đổi thời gian event kiểm lại mọi hoạt động.

## QR và check-in

BR_SUTI_04: mỗi event chỉ một QR hiệu lực, check-in chỉ trong thời gian event. Chỉ sinh QR cho event đã duyệt còn đủ điều kiện. Sinh lại thu hồi mã cũ trong cùng transaction/constraint thích hợp; hai request không tạo hai mã active.

Token khó đoán, hash/HMAC theo rule bảo mật. Check-in kiểm token/active/expiresAt/trạng thái/thời gian event/danh tính khách. expiresAt mẫu sau endAt không được nới cửa sổ check-in.

Replay/quét đồng thời không ghi lượt/thưởng trùng. Đề xuất uniqueness event+customer nếu mỗi khách một lượt; tái nhập cần quyết định riêng. Không chỉ dùng qr+customer nếu QR mới cho cùng khách thưởng lại. Thưởng F11 có idempotency, không xây hệ thống thành viên đầy đủ.

## Khuyến mãi và voucher

Một chương trình đúng một loại DISCOUNT, VOUCHER_GIVEAWAY, POINT_ACCUMULATION theo mô hình hoặc mã API đã map; không lưu cả ba subtype. Validate ngày/hóa đơn tối thiểu không âm/hạng khách/cửa hàng/đơn vị ưu đãi. Khi apply thật, dữ liệu hóa đơn xác minh từ nguồn, không tin tiền client khai.

Voucher code unique, viết hoa, không ký tự đặc biệt theo phân tích; wireframe/ví dụ có gạch ngang/gạch dưới trái mô tả nên phải chốt allowlist. Phần trăm >0 và <=100, giá trị tiền hợp lệ, maximumDiscount theo loại, validFrom < expiresAt, perCustomerLimit hợp lệ. Chính sách cộng dồn chưa có thì không tự đặt.

**issuedLimit=0 là không giới hạn**, không phải hết kho. issuedCount khác usedCount, DB là nguồn kiểm chứng. Sửa hạn mức không thấp hơn số đã phát nếu chưa có quy trình thu hồi; giảm hạn dùng phải quyết định ảnh hưởng voucher đã cấp. Issue/apply/redeem có tham chiếu/idempotency; cạnh tranh không vượt tổng/per-customer limit.

Redis DECR theo rule 03, không là chứng cứ MySQL đã commit, số còn lại không âm. Voucher ISSUED/USED/EXPIRED/REVOKED chuyển có điều kiện, không redeem hai lần/dùng voucher người khác. Điểm/quy đổi/làm tròn từ cấu hình, không mặc định ví dụ 1.000 VNĐ = 1 điểm. Ngưỡng cảnh báo sắp hết kho phải có cấu hình/nguồn, không bịa.

## Báo cáo và công khai

Tổng chi phí từ bản ghi thực tế hợp lệ; chênh lệch = approvedBudget - totalActualExpense. Ghi rõ đếm khách duy nhất hay lượt vào. Tỷ lệ tham dự chỉ khi targetAttendees >0. JOIN nhiều bảng con không được nhân bản chi phí/lượt, dùng tổng hợp đúng.

ROI theo Word: (doanh thu quy thuộc - chi phí thực tế)/chi phí thực tế. Chỉ khi doanh thu xác minh và chi phí >0; thiếu dữ liệu trả null/trạng thái chưa xác định/lý do. Không copy 24.5% mẫu hoặc dùng ngân sách làm doanh thu. Event chưa diễn ra hiển thị chưa có tổng kết, phân biệt 0 thật.

Public search: publicVisible, chưa archived, event đã duyệt sắp/đang diễn ra, promotion sắp/đang hiệu lực theo trạng thái đã chọn. Không nháp/chờ duyệt/từ chối/hủy/đã kết thúc. Phân trang hợp nhất EVENT/PROMOTION sort ổn định/total đúng, không phân trang riêng rồi ghép sai.

## Kiểm chứng

Sai quyền/version duyệt; file sai/quá mức; lịch cạnh tranh/ngoài event; QR cạnh tranh/hết hạn/ngoài giờ; check-in trùng; voucher cuối cùng cạnh tranh/unlimited=0/per-customer/redeem/apply lặp; Redis/DB lỗi giữa bước; báo cáo JOIN không nhân số; chi phí/mục tiêu 0; thiếu doanh thu; publicVisible=false.
