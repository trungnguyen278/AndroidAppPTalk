# 📱 Hướng dẫn sử dụng ứng dụng PTalk Config

## Mục lục
1. [Giới thiệu](#1-giới-thiệu)
2. [Yêu cầu hệ thống](#2-yêu-cầu-hệ-thống)
3. [Cài đặt ứng dụng](#3-cài-đặt-ứng-dụng)
4. [Bắt đầu sử dụng](#4-bắt-đầu-sử-dụng)
5. [Quét thiết bị](#5-quét-thiết-bị)
6. [Cấu hình thiết bị](#6-cấu-hình-thiết-bị)
7. [Xử lý sự cố](#7-xử-lý-sự-cố)

---

## 1. Giới thiệu

**PTalk Config** là ứng dụng cấu hình thiết bị PTalk được phát triển bởi Học viện Công nghệ Bưu chính Viễn thông (PTIT). Ứng dụng cho phép bạn:
- Quét tìm thiết bị PTalk qua Bluetooth
- Cấu hình WiFi cho thiết bị
- Điều chỉnh âm lượng và độ sáng ban đầu

---

## 2. Yêu cầu hệ thống

| Yêu cầu | Chi tiết |
|---------|----------|
| Hệ điều hành | Android 8.0 (API 26) trở lên |
| Bluetooth | Bluetooth LE (4.0 trở lên) |
| Quyền cần thiết | Bluetooth Scan, Bluetooth Connect, Vị trí |

---

## 3. Cài đặt ứng dụng

1. Tải file APK từ nguồn được cung cấp
2. Mở file APK trên điện thoại Android
3. Cho phép cài đặt từ nguồn không xác định (nếu được yêu cầu)
4. Nhấn **Cài đặt** và đợi quá trình hoàn tất
5. Nhấn **Mở** để khởi chạy ứng dụng

---

## 4. Bắt đầu sử dụng

### 4.1 Màn hình chính

Khi mở ứng dụng, bạn sẽ thấy màn hình chính với:
- **Logo PTIT** - Học viện Công nghệ Bưu chính Viễn thông
- **Hướng dẫn sử dụng** gồm 4 bước:
  1. **Quét thiết bị** - Nhấn nút "Bắt đầu cấu hình"
  2. **Xem trên radar** - Thiết bị xuất hiện trên màn hình radar
  3. **Chọn thiết bị** - Nhấn vào biểu tượng "P" màu xanh
  4. **Cấu hình WiFi** - Nhập thông tin WiFi

### 4.2 Cấp quyền

Khi nhấn **Bắt đầu cấu hình** lần đầu, ứng dụng sẽ yêu cầu các quyền:
- **Bluetooth Scan**: Để quét tìm thiết bị PTalk
- **Bluetooth Connect**: Để kết nối với thiết bị  
- **Vị trí**: Yêu cầu bởi Android để quét Bluetooth

⚠️ **Lưu ý**: Cần cấp đủ quyền để ứng dụng hoạt động.

---

## 5. Quét thiết bị

### 5.1 Màn hình quét (Radar)

Sau khi nhấn **Bắt đầu cấu hình**, bạn sẽ thấy màn hình radar:
- **Radar xanh** - Hiển thị như radar trong phim
- **Vòng sweep** - Quét liên tục khi đang tìm kiếm
- **Các điểm "P"** - Đại diện cho thiết bị PTalk tìm thấy

### 5.2 Cách đọc radar

| Vị trí trên radar | Ý nghĩa |
|-------------------|---------|
| Gần tâm | Tín hiệu mạnh (thiết bị gần) |
| Xa tâm | Tín hiệu yếu (thiết bị xa) |

### 5.3 Bắt đầu quét

1. Đảm bảo thiết bị PTalk đang **BẬT** và ở chế độ **cấu hình**
2. Nhấn nút **Quét thiết bị** 
3. Đợi radar tìm kiếm thiết bị xung quanh
4. Thiết bị tìm thấy sẽ hiển thị dưới dạng điểm "P" màu xanh

### 5.4 Chọn thiết bị

1. **Nhấn vào biểu tượng "P"** của thiết bị bạn muốn cấu hình
2. Đợi ứng dụng kết nối (hiển thị "Đang kết nối thiết bị...")
3. Cửa sổ cấu hình sẽ tự động mở khi kết nối thành công

---

## 6. Cấu hình thiết bị

### 6.1 Cửa sổ cấu hình

Sau khi kết nối thành công, cửa sổ cấu hình hiển thị:

#### A. Device ID
- Hiển thị ID duy nhất của thiết bị
- Được tự động đọc từ thiết bị

#### B. Cấu hình WiFi

**Tên WiFi (SSID)**
- Danh sách WiFi được **tự động tải** khi mở cửa sổ
- Nhấn vào ô để xem danh sách mạng WiFi
- Hoặc nhập trực tiếp tên WiFi

💡 **Mẹo**: Nếu danh sách trống, nhấn nút **Làm mới** 🔄

**Mật khẩu WiFi**
- Nhập mật khẩu của mạng WiFi đã chọn
- Nhấn biểu tượng 👁️ để hiển thị/ẩn mật khẩu

⚠️ **Lưu ý quan trọng**:
- Thiết bị chỉ hỗ trợ **WiFi 2.4GHz** (không hỗ trợ 5GHz)
- Đảm bảo nhập đúng mật khẩu

#### C. Âm lượng (Mặc định: 60%)
- Kéo thanh trượt để điều chỉnh
- Phạm vi: 0% - 100%

#### D. Độ sáng màn hình (Mặc định: 100%)
- Kéo thanh trượt để điều chỉnh
- Phạm vi: 0% - 100%

### 6.2 Lưu cấu hình

1. Điền đầy đủ thông tin WiFi
2. Điều chỉnh âm lượng và độ sáng (tùy chọn)
3. Nhấn nút **Lưu cấu hình**
4. Đợi thông báo **"Cấu hình thiết bị hoàn tất!"**
5. Nhấn **Hoàn thành**

### 6.3 Hủy cấu hình

- Nhấn nút **X** ở góc trên phải để đóng cửa sổ
- Nhấn **Hủy** để quay lại màn hình quét

---

## 7. Xử lý sự cố

### 7.1 Không tìm thấy thiết bị khi quét

**Nguyên nhân có thể**:
- Bluetooth điện thoại chưa bật
- Thiết bị PTalk chưa ở chế độ cấu hình
- Khoảng cách quá xa

**Giải pháp**:
1. Bật Bluetooth trên điện thoại
2. Đặt thiết bị PTalk vào chế độ pairing mode
3. Di chuyển gần thiết bị hơn (< 3m)
4. Nhấn **Quét thiết bị** lại

### 7.2 Không kết nối được với thiết bị

**Nguyên nhân có thể**:
- Thiết bị đã kết nối với điện thoại khác
- Lỗi Bluetooth

**Giải pháp**:
1. Khởi động lại thiết bị PTalk
2. Tắt/bật Bluetooth điện thoại
3. Khởi động lại ứng dụng
4. Thử kết nối lại

### 7.3 Danh sách WiFi trống

**Nguyên nhân có thể**:
- Kết nối BLE chưa ổn định
- Thiết bị chưa quét xong WiFi

**Giải pháp**:
1. Nhấn nút **Làm mới** 🔄
2. Đợi vài giây và thử lại
3. Nếu vẫn trống, nhập trực tiếp tên WiFi

### 7.4 Lỗi "Không thể kết nối với thiết bị"

**Giải pháp**:
1. Kiểm tra thiết bị PTalk còn bật không
2. Đảm bảo khoảng cách < 5m
3. Thử kết nối lại từ đầu
4. Khởi động lại thiết bị PTalk

### 7.5 Thiết bị không kết nối được WiFi sau cấu hình

**Nguyên nhân có thể**:
- Sai mật khẩu WiFi
- Mạng WiFi 5GHz (không hỗ trợ)
- Tín hiệu WiFi yếu

**Giải pháp**:
1. Kiểm tra lại mật khẩu WiFi
2. Sử dụng mạng WiFi **2.4GHz**
3. Đặt thiết bị gần router hơn
4. Cấu hình lại WiFi cho thiết bị

---

## Thông tin liên hệ

**Học viện Công nghệ Bưu chính Viễn thông (PTIT)**
- Website: https://ptit.edu.vn
- Địa chỉ: Km10, Đường Nguyễn Trãi, Q. Hà Đông, TP. Hà Nội

---

*Phiên bản tài liệu: 2.0 - Cập nhật: Tháng 2/2026*

