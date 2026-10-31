# D0005 – Ma trận chuyển trạng thái mặt bằng/cửa hàng (F01)

- **Ngày**: 31/10/2026
- **Nguồn**: rule `code/rules/f01-quan-ly-mat-bang.md` (PlantUML PremiseStatus/StoreStatus; DANG_THUE/DA_THUE/CHO_THUE chưa rõ nghĩa), TK-F01 mục 3.7.

## Quyết định

Ma trận dưới đây là **đề xuất baseline** cần nghiệp vụ chốt lại; được cài đặt tập trung tại `f01/constant/StatusTransition` và phủ bởi test. Mã dùng theo D0001.

| Từ \ Đến | VACANT | RESERVED | LEASED | MAINTENANCE | SUSPENDED | RETIRED |
| --- | --- | --- | --- | --- | --- | --- |
| VACANT | – | ✓ | ✓ | ✓ | ✓ | ✓ |
| RESERVED | ✓ | – | ✓ | ✓ | ✓ | ✗ |
| LEASED | ✓* | ✓ | – | ✓ | ✓ | ✓ |
| MAINTENANCE | ✓ | ✓ | ✓ | – | ✓ | ✓ |
| SUSPENDED | ✓ | ✓ | ✓ | ✓ | – | ✓ |
| RETIRED | ✗ | ✗ | ✗ | ✗ | ✗ | – |

`✓*`: LEASED → VACANT chỉ cho phép khi **không còn hợp đồng hiệu lực** (kiểm qua `ContractQueryPort`, rule f01: hợp đồng kết thúc chưa đủ đặt VACANT nếu còn hợp đồng/liên kết khác hoặc đang bảo trì/ngừng khai thác). RETIRED là trạng thái kết thúc, không chuyển tiếp. RESERVED không sang RETIRED (chưa khai thác, thu hồi về VACANT).

Áp dụng chung cho cả PremiseStatus và StoreStatus (hai tập trùng giá trị theo PlantUML).

## Điều kiện kèm theo

- Chuyển trạng thái luôn ghi lịch sử cùng transaction (`UPDATE_STATUS`).
- Không đổi tay thành trạng thái trái hợp đồng (LEASED ↔ VACANT kiểm hợp đồng trước).
- Chuyển trái ma trận → 409 BUSINESS_CONFLICT (`errorCode=INVALID_STATUS_TRANSITION` trong `errors`).
