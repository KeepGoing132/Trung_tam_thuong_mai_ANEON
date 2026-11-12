# Luồng tài khoản ANEON

## Vai trò và điểm vào

| Người dùng | Màn hình | Luồng |
| --- | --- | --- |
| Khách mua sắm | `/tai-khoan` hoặc liên kết `/check-in` | Đăng ký bằng tên đăng nhập, họ tên và mật khẩu. Email là tùy chọn. Backend tạo hồ sơ khách và gắn vai trò `CUSTOMER` trong cùng giao dịch. Khách dùng tài khoản đó để tự check-in. |
| Nhân viên | `/quan-ly` | Đăng nhập bằng tài khoản do quản trị cấp. Đổi mật khẩu tạm trước khi dùng các nghiệp vụ F01/F10. |
| Quản trị hệ thống | `/quan-ly` → **Tài khoản nhân viên** | Tạo tài khoản nhân viên theo vai trò, khóa/mở khóa và cấp lại mật khẩu tạm. Quản trị được tạo riêng khi khởi động lần đầu. |

## Đăng nhập và phiên

1. Frontend gửi `POST /api/v1/auth/login` qua proxy Next.js với tên đăng nhập và mật khẩu. Tên được chuẩn hóa chữ thường. API trả thông báo chung khi tài khoản thiếu, bị khóa hoặc mật khẩu sai.
2. Backend kiểm tra giới hạn thử theo tài khoản và địa chỉ kết nối, xác minh mật khẩu PBKDF2 có salt, rồi sinh token phiên 256 bit. MySQL lưu SHA-256 của token và phiên bản xác thực tài khoản. Cookie `aneon_session` dùng `HttpOnly`, `SameSite=Lax`, tồn tại tối đa 8 giờ.
3. Khi tải lại hoặc chuyển trang, frontend gọi `GET /api/v1/auth/me`. Backend lấy vai trò từ MySQL và nạp quyền F01/F10 theo `RolePermissions`. Vai trò do người dùng nhập trên form đăng ký hoặc đăng nhập không cấp quyền cho họ.
4. Các yêu cầu ghi sử dụng cookie phải có header `X-ANEON-Request: aneon-web`. Backend từ chối yêu cầu trình duyệt đánh dấu `cross-site`. Khi triển khai qua HTTPS, đặt `ANEON_AUTH_SECURE_COOKIES=true` và phục vụ frontend/backend qua cùng origin.
5. `POST /api/v1/auth/logout` thu hồi phiên hiện tại và xóa cookie. Đổi mật khẩu, đặt lại mật khẩu hoặc khóa tài khoản thu hồi các phiên đang tồn tại. Phiên của tài khoản buộc đổi mật khẩu tạm chỉ truy cập được các thao tác cần thiết để đổi mật khẩu hoặc đăng xuất.

## Khôi phục mật khẩu

- Khi còn đăng nhập, vào **Tài khoản → Mã khôi phục**, xác nhận mật khẩu rồi lưu mã được hiển thị. MySQL chỉ lưu hash và mỗi tài khoản giữ một mã có hiệu lực tại một thời điểm.
- Khi quên mật khẩu, chọn **Quên mật khẩu**, nhập tên đăng nhập, mã đã lưu và mật khẩu mới. Mã được tiêu thụ trong cùng giao dịch đổi mật khẩu. Sau đó đăng nhập lại và tạo mã mới.
- Nhân viên có thể nhờ quản trị cấp lại mật khẩu tạm nếu mất mã. Khôi phục qua email và xác minh email cần dịch vụ xác minh riêng.

## API

| Phương thức và đường dẫn | Kết quả |
| --- | --- |
| `POST /api/v1/auth/register` | Tạo khách và mở phiên |
| `POST /api/v1/auth/login` | Mở phiên mới |
| `GET /api/v1/auth/me` | Đọc tài khoản trong phiên |
| `POST /api/v1/auth/logout` | Thu hồi phiên hiện tại |
| `PUT /api/v1/auth/password` | Đổi mật khẩu và mở phiên mới |
| `POST /api/v1/auth/recovery-code` | Sinh mã khôi phục sau khi xác nhận mật khẩu |
| `POST /api/v1/auth/recover` | Tiêu thụ mã và đặt lại mật khẩu |
| `GET /api/v1/auth/staff` | Danh sách nhân viên có phân trang |
| `POST /api/v1/auth/staff` | Tạo nhân viên, trả mật khẩu tạm |
| `PUT /api/v1/auth/staff/{id}/status` | Khóa hoặc mở khóa nhân viên |
| `POST /api/v1/auth/staff/{id}/password-reset` | Thu hồi mật khẩu cũ và cấp mật khẩu tạm mới |

Các API quản trị yêu cầu phiên ANEON và vai trò `SYSTEM_ADMIN`. Luồng JWT ngoài hệ thống và header test vẫn phục vụ các API F01/F10 đã có khi được bật rõ ràng. Bản đăng nhập bằng mật khẩu cấp quyền theo vai trò hiện có, với phạm vi toàn trung tâm cho nhân viên.

## Khởi tạo quản trị trên máy phát triển

1. Chọn tên đăng nhập và một mật khẩu tạm riêng có ít nhất 12 ký tự. Đặt `ANEON_BOOTSTRAP_ADMIN_USERNAME` và `ANEON_BOOTSTRAP_ADMIN_PASSWORD` trước lần khởi động backend đầu tiên. Flyway V5 cập nhật schema, sau đó ứng dụng tạo tài khoản `SYSTEM_ADMIN` nếu tên chưa tồn tại. Lần khởi động sau giữ tài khoản đã tạo.
2. Mở `/quan-ly`, đăng nhập và đổi mật khẩu tạm. Xóa hai biến môi trường bootstrap khỏi cấu hình khởi động sau khi cấp xong tài khoản.
3. Trong **Tài khoản nhân viên**, tạo một tài khoản Ban quản lý hoặc Marketing. Đăng nhập bằng tài khoản đó để thực hiện các thao tác theo vai trò. Tài khoản mẫu từ seed vẫn dùng được trong chế độ UUID thử nghiệm khi được bật ở cả backend và frontend.
