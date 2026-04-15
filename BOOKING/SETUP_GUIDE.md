# Configuration Guide - Danangbest Tour Booking System

## 📋 Tệp Cấu Hình

File `application.properties` đã được tạo tại:
```
backend/src/main/resources/application.properties
```

## 🔧 Cấu Hình Cần Thiết

Trước khi chạy backend, hãy cập nhật các giá trị sau trong `application.properties`:

### 1. 🗄️ Database (SQL Server)
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=tourbooking
spring.datasource.username=sa
spring.datasource.password=YourPasswordHere
```

**Hướng dẫn**:
- Đảm bảo SQL Server đang chạy
- Tạo database tên `tourbooking` hoặc sử dụng tên database hiện có
- Cập nhật username/password của SQL Server

### 2. 🔐 JWT Secret
```properties
app.jwt.secret=your-secret-key-here-make-it-long-and-secure-at-least-64-characters-for-hs256
```

**Hướng dẫn**:
- Đây là khóa bí mật để ký JWT tokens
- Cần dài (tối thiểu 64 ký tự) để an toàn
- Cập nhật với giá trị an toàn của bạn

### 3. 💳 PayOS (Thanh Toán)
```properties
payos.client-id=your-payos-client-id
payos.api-key=your-payos-api-key
payos.checksum-key=your-payos-checksum-key
```

**Hướng dẫn**:
- Lấy từ tài khoản PayOS của bạn
- Dùng cho xử lý thanh toán

### 4. 📧 Email (Gmail)
```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

**Hướng dẫn**:
- Để nhận email từ Gmail, bạn cần:
  1. Bật 2FA trên Gmail
  2. Tạo App Password tại https://myaccount.google.com/apppasswords
  3. Sử dụng mật khẩu ứng dụng (không phải mật khẩu Gmail)

### 5. 🔑 Google OAuth
```properties
spring.security.oauth2.client.registration.google.client-id=your-google-client-id
spring.security.oauth2.client.registration.google.client-secret=your-google-client-secret
```

**Hướng dẫn**:
- Tạo OAuth credentials từ https://console.cloud.google.com
- Cần cho đăng nhập bằng Google

### 6. 🗺️ OpenTripMap (Tùy chọn)
```properties
opentripmap.scheduler-max-cities-per-run=5
opentripmap.scheduler-cities=Da Nang,Ho Chi Minh,Hanoi
```

## 🚀 Chạy Backend

```bash
cd backend
../mvnw.cmd spring-boot:run
```

hoặc từ thư mục gốc BOOKING:

```bash
mvnw.cmd -pl backend spring-boot:run
```

Backend sẽ chạy tại: `http://localhost:8080/api`

## 📝 Ghi Chú

- Một số cấu hình có thể bị bỏ qua nếu bạn không dùng PayOS hoặc Google OAuth
- Spring Boot sẽ bỏ qua các thuộc tính không cần thiết
- Nếu gặp lỗi kết nối database, kiểm tra:
  - SQL Server đang chạy?
  - URL, username, password chính xác?
  - Database đã được tạo?

