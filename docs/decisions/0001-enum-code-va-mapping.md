# D0001 – Mã enum nội bộ và mapping tiếng Việt

- **Ngày**: 31/10/2026
- **Nguồn**: rule `code/rules/00-boi-canh-va-nguon.md` (điểm lệch enum), `../tai_lieu_pha_phan_tich/class_diagram_thuc_the_toan_he_thong_tieng_viet.puml`, TK-F01 mục 1.3/3.6, TK-F10 mục 1.3.

## Quyết định

Mã enum nội bộ và giá trị API lấy theo mô hình PlantUML (tiếng Anh, snake-case). Nhãn tiếng Việt chỉ dùng cho hiển thị/tài liệu, không dùng làm giá trị API.

| Tập | Giá trị API |
| --- | --- |
| PremiseStatus | VACANT, RESERVED, LEASED, MAINTENANCE, SUSPENDED, RETIRED |
| StoreStatus | VACANT, RESERVED, LEASED, MAINTENANCE, SUSPENDED, RETIRED |
| PremiseType | STORE, KIOSK, ADVERTISING, EVENT_AREA, SERVICE_AREA, OTHER |
| EventStatus | DRAFT, PENDING_APPROVAL, APPROVED, REJECTED, ONGOING, FINISHED, CANCELLED |
| PromotionType | DISCOUNT, VOUCHER_GIVEAWAY, POINT_ACCUMULATION |
| PromotionStatus | DRAFT, SCHEDULED, ACTIVE, FINISHED, CANCELLED |
| VoucherDiscountType | FIXED_AMOUNT, PERCENTAGE |
| VoucherIssueStatus | ISSUED, USED, EXPIRED, REVOKED |

Bộ tiếng Việt trong TK-F01 (TRONG/DANG_THUE/DA_THUE/BAO_TRI/NGUNG_KHAI_THAC; TRONG/DANG_SUA_CHUA/CHO_THUE/DA_THUE/TAM_NGUNG) **không được nhận làm giá trị API** và **không tự dịch một-một** sang mã PlantUML: rule 00 ghi rõ DANG_THUE/DA_THUE/CHO_THUE chưa đủ nghĩa để coi là RESERVED/LEASED/VACANT. Gửi giá trị không thuộc bảng trên trả 400 VALIDATION_ERROR.

## Ảnh hưởng

- Converter enum của Jackson chỉ nhận mã PlantUML; có contract test chặn giá trị tiếng Việt.
- Khi nghiệp vụ chốt ý nghĩa bộ tiếng Việt, bổ sung mapping mới vào converter và cập nhật quyết định này.
