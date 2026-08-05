# FlowerShop API (SoulFlow)

Đây là mã nguồn Backend API cho ứng dụng thương mại điện tử FlowerShop (SoulFlow). Hệ thống được xây dựng bằng Spring Boot, cung cấp một API RESTful mạnh mẽ, dễ mở rộng và bảo mật cao để quản lý cửa hàng hoa trực tuyến.

## Công nghệ sử dụng (Tech Stack)

- **Ngôn ngữ:** Java 17
- **Framework:** Spring Boot 3.3.5
- **Cơ sở dữ liệu (Database):** Microsoft SQL Server
- **Bộ nhớ đệm (Caching):** Redis (Spring Data Redis)
- **Bảo mật (Security):** Spring Security + JWT, đăng nhập Google OAuth2
- **Lưu trữ file:** MinIO (Object Storage)
- **Mapping Data:** MapStruct
- **Realtime:** WebSockets (Spring WebSocket)

## Các tính năng chính

- **Xác thực & Phân quyền:** Đăng nhập bảo mật bằng JWT và tích hợp Google OAuth2. Phân quyền truy cập rõ ràng giữa User và Admin.
- **Quản lý Sản phẩm:** Hỗ trợ đầy đủ các thao tác CRUD cho sản phẩm (hoa), danh mục và theo dõi số lượng tồn kho.
- **Xử lý Đơn hàng:** Quản lý giỏ hàng, thanh toán và theo dõi đơn hàng.
- **Cập nhật Đơn hàng Realtime:** Sử dụng WebSockets để bắn thông báo trạng thái đơn hàng theo thời gian thực xuống Client (cả User và Admin) mỗi khi có thay đổi.
- **Tích hợp Thanh toán:** Tích hợp webhook của SePay để tự động cập nhật trạng thái đơn hàng khi khách hàng chuyển khoản ngân hàng thành công.
- **Hệ thống Caching:** Tối ưu hóa hiệu năng bằng cách dùng Redis để cache danh sách sản phẩm, danh sách đơn hàng và các truy vấn thường xuyên.
- **Bình luận & Đánh giá:** Người dùng có thể để lại bình luận và đánh giá về sản phẩm.

## Cập nhật gần đây

- **Sửa lỗi (Bug Fix):** Đã khắc phục sự cố trong `OrderMapper` gây ra lỗi `DataIntegrityViolationException` khi cập nhật trạng thái đơn hàng do ánh xạ sai cột `del_if`.
- **Đồng bộ Realtime:** Đã triển khai tính năng gửi thông báo qua WebSocket (vào kênh `/topic/order.{mã_đơn_hàng}` và `/topic/user.notifications.{username}`). Tính năng này giúp giao diện của khách hàng tự động cập nhật ngay lập tức khi Admin thay đổi trạng thái đơn hàng mà không cần phải tải lại trang (F5).

## Hướng dẫn cài đặt và chạy (Getting Started)

### Yêu cầu hệ thống
- JDK 17
- Microsoft SQL Server
- Redis Server
- MinIO Server
- Maven (hoặc dùng `mvnw` có sẵn trong project)

### Cách chạy ứng dụng
1. **Clone mã nguồn về máy.**
2. **Cấu hình Môi trường:** Tạo hoặc cập nhật file `.env` (hoặc `application.properties`) với các thông tin đăng nhập database, Redis, MinIO và Google OAuth2 (bạn có thể tham khảo từ file `.env.example`).
3. **Chạy Project:**
   Bạn có thể dùng câu lệnh sau:
   ```bash
   ./mvnw.cmd spring-boot:run
   ```
   Hoặc chạy bằng Docker Compose (nếu đã config sẵn):
   ```bash
   docker-compose up -d
   ```
4. API sẽ chạy tại địa chỉ: `http://localhost:8080`.

## Tài liệu API (API Documentation)

- **API dành cho User:** `/user/**` (Yêu cầu phải đăng nhập)
- **API dành cho Admin:** `/admin/**` (Yêu cầu quyền Admin)
- **WebSockets:** Endpoint để kết nối là `/ws`. Các kênh (topics) bao gồm `/topic/admin.notifications`, `/topic/user.notifications.{username}`, và `/topic/order.{code}`.

## Giấy phép (License)
Dự án được cấp phép theo tiêu chuẩn MIT License.
