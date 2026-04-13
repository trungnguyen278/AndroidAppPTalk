# PTalk Assistant (AndroidAppPTalk)

## Giới thiệu (Overview)
**PTalk Assistant** là ứng dụng Android được phát triển dành cho việc quản lý và cấu hình các thiết bị IoT (nền tảng của Học viện Công nghệ Bưu chính Viễn thông - PTIT). Ứng dụng cung cấp giải pháp toàn diện giúp người dùng có thể giao tiếp, cấu hình mạng thiết bị ban đầu thông qua Bluetooth Low Energy (BLE) và giám sát, điều khiển trạng thái thiết bị từ xa qua đám mây (Cloud IoT Platform).

## Tính năng nổi bật (Key Features)
- **Quản lý người dùng**: Hỗ trợ Đăng nhập, Đăng ký và làm mới Token tự động qua hệ thống API của nền tảng IoT.
- **Cấu hình thiết bị (BLE Provisioning)**: Quét và cấu hình Wi-Fi cho các thiết bị IoT thông qua kết nối Bluetooth hiện đại.
- **Quản lý thiết bị**: Xem danh sách các thiết bị hiện có, cập nhật thông tin và xóa bỏ các thiết bị khỏi tài khoản.
- **Điều khiển thời gian thực**: Giao tiếp, theo dõi và điều khiển thiết bị nhanh chóng với độ trễ cực thấp thông qua giao thức MQTT.
- **Giao diện hiện đại**: Giao diện người dùng được xây dựng 100% bằng Jetpack Compose với các hiệu ứng bắt mắt, hỗ trợ giao diện sáng/tối (Light/Dark mode) đồng bộ.

## Công nghệ sử dụng (Tech Stack)
Dự án áp dụng các tiêu chuẩn và công nghệ hiện đại nhất của lập trình Android:
- **Ngôn ngữ**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
- **Kiến trúc**: MVVM (Model - View - ViewModel) Clean Architecture.
- **Dependency Injection**: [Dagger-Hilt](https://dagger.dev/hilt/)
- **Mạng (Network)**: [Retrofit](https://square.github.io/retrofit/), OkHttp3 để giao tiếp REST API.
- **Real-time**: Protocol MQTT v5 với [Eclipse Paho](https://www.eclipse.org/paho/).
- **Lưu trữ nội bộ (Local Storage)**: [Room Database](https://developer.android.com/training/data-storage/room) & DataStore Preferences.
- **Thiết kế điều hướng**: Compose Navigation.

## Yêu cầu cấu hình (Requirements)
Để build và chạy dự án, bạn sẽ cần:
- **Android Studio** Iguana/Jellyfish (hoặc các phiên bản mới nhất hỗ trợ Gradle 8+).
- **Thiết bị thật** (khuyến nghị khi test các tính năng liên quan đến BLE) hoặc Emulator.
- Quyền ứng dụng bao gồm: Internet, Bluetooth (Scan, Connect, Admin), và Location (coarse/fine).

## Cài đặt và Chạy thử (Installation & Setup)
1. **Clone repository về máy:**
   ```bash
   git clone <repo_url>
   ```
2. **Mở dự án:**
   Khởi động Android Studio, chọn **Open** và trỏ đến thư mục `AndroidAppPTalk`.
3. **Đồng bộ Gradle:**
   Đợi Android Studio tải xuống các thư viện (`libs.versions.toml`) và đồng bộ hệ thống tệp.
4. **Cấp quyền:**
   Do tính năng quét Bluetooth, trên Android 12 trở lên bạn cần cấp quyền Nearby Devices (Thiết bị lân cận) và quyền Vị trí.
5. **Chạy ứng dụng:**
   Kết nối thiết bị, chọn cấu hình module `app` và nhấn **Run** (Shift + F10). 

## Cấu trúc thư mục chính (Key Architecture)
```text
com.avis.app.ptalk
├── core
│   ├── network/      # Quản lý giao tiếp API với IoT Platform (Endpoints, TokenManager)
│   └── mqtt/         # Quản lý Connection và Publisher/Subscriber MQTT
├── di             # Các file cấu hình Dagger Hilt Modules (NetworkModule, DatabaseModule...)
├── domain
│   └── model/        # Data classes đại diện cho User, Device, v.v.
├── ui
│   ├── screen/       # Các Jetpack Compose UI Functions (Home, Đăng nhập, Quản lý thiết bị...)
│   ├── viewmodel/    # Các StateHolder (ViewModels) kết nối UI với Data/Core
│   └── theme/        # Định nghĩa màu sắc (AppColors, TechColors), Typography, Styles
└── navigation     # Xây dựng NavHost / ConfigNavGraph cho sự luân chuyển giữa các màn hình
```

## Đóng góp (Contributing)
Mọi Pull Request nhằm mục đích cải thiện giao diện, sửa lỗi (bug fixes) hay bổ sung tính năng mới đều được hoan nghênh. Lưu ý tuân thủ đúng kiến trúc MVVM và quy chuẩn code Kotlin hiện tại của dự án.

## Giấy phép (License)
Dự án được xây dựng phục vụ cho mục đích nghiên cứu nền tảng IoT và học tập tại **Học viện Công nghệ Bưu chính Viễn thông (PTIT)**.
