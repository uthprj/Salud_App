# Salud - Trợ lý Sức khỏe Cá nhân 🥗🏃‍♂️💤

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg?logo=kotlin)](https://kotlinlang.org) [![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-2024.09.00-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose) [![Nền tảng](https://img.shields.io/badge/Nền_tảng-Android-3DDC84.svg?logo=android)](https://www.android.com/)

**Salud** là một ứng dụng di động dành cho Android, được xây dựng với mục tiêu trở thành một người trợ lý sức khỏe toàn diện, giúp người dùng dễ dàng theo dõi, quản lý và cải thiện lối sống hằng ngày.

<br>

<p align="center">
  
  <img src="https://github.com/uthprj/Salud_App/blob/main/logo.png?raw=true" alt="Salud App Mockup" width="80%">
</p>

<br>

## Mục lục
- [Salud - Trợ lý Sức khỏe Cá nhân 🥗🏃‍♂️💤](#salud---trợ-lý-sức-khỏe-cá-nhân-️)
  - [Mục lục](#mục-lục)
  - [1. Lý do ra đời](#1-lý-do-ra-đời)
  - [2. Đối tượng người dùng](#2-đối-tượng-người-dùng)
  - [3. Các tính năng chính](#3-các-tính-năng-chính)
  - [4. Công nghệ sử dụng](#4-công-nghệ-sử-dụng)
  - [5. Hướng phát triển](#5-hướng-phát-triển)

## 1. Lý do ra đời

Trong xã hội hiện đại, việc duy trì một lối sống lành mạnh ngày càng trở nên quan trọng nhưng cũng đầy thách thức. Người dùng thường gặp khó khăn trong việc theo dõi các chỉ số sức khỏe, quản lý dinh dưỡng, và duy trì lịch trình luyện tập do thiếu một công cụ hỗ trợ trực quan và hiệu quả.

➡️ **Salud** được phát triển để giải quyết vấn đề này, cung cấp một nền tảng "tất cả trong một" để quản lý sức khỏe một cách khoa học và dễ dàng.

## 2. Đối tượng người dùng

Salud hướng đến một tệp người dùng đa dạng, bao gồm:
- **Sinh viên & Nhân viên văn phòng**: Những người bận rộn cần một công cụ nhanh gọn để theo dõi sức khỏe tổng quan.
- **Người tập gym, fitness**: Những người muốn theo dõi chế độ ăn, lượng calo và lịch trình tập luyện một cách chi tiết.
- **Người quan tâm sức khỏe**: Bất cứ ai muốn theo dõi các chỉ số cơ thể (cân nặng, huyết áp,...) một cách định kỳ để phòng ngừa bệnh tật.

## 3. Các tính năng chính

| Tính năng                | Mô tả                                                                                         | Trạng thái        |
| :------------------------ | :-------------------------------------------------------------------------------------------- | :---------------- |
| 👤 **Quản lý Tài khoản**    | Đăng ký, đăng nhập an toàn để cá nhân hóa trải nghiệm.                                        | ✅ Hoàn thành     |
| 📊 **Dashboard Tổng quan**  | Hiển thị các chỉ số quan trọng ngay màn hình chính: cân nặng, BMI, calo trong ngày.            | ✅ Hoàn thành     |
| ❤️ **Theo dõi Sức khỏe**   | Ghi nhận và trực quan hóa các chỉ số (cân nặng, huyết áp, nhịp tim) qua biểu đồ theo thời gian. | ⏳ Đang phát triển |
| 🥗 **Quản lý Dinh dưỡng**  | Theo dõi lượng calo nạp vào từ các bữa ăn, tìm kiếm thực phẩm và xây dựng thực đơn.              | ⏳ Đang phát triển |
| 🏃‍♂️ **Quản lý Vận động**    | Ghi nhận các hoạt động thể chất, theo dõi thời lượng và lượng calo tiêu thụ.                   | 📝 Lên kế hoạch   |
| 💤 **Theo dõi Giấc ngủ**    | Ghi nhận thời gian ngủ và thức dậy, đánh giá chất lượng giấc ngủ.                               | 📝 Lên kế hoạch   |
| 🎯 **Thiết lập Mục tiêu**   | Đặt ra các mục tiêu sức khỏe (ví dụ: giảm 5kg) và theo dõi tiến trình thực hiện.                | 📝 Lên kế hoạch   |


## 4. Công nghệ sử dụng

Dự án được xây dựng hoàn toàn bằng các công nghệ hiện đại và phổ biến trong hệ sinh thái Android.

- **Ngôn ngữ**: [Kotlin](https://kotlinlang.org/) (Sử dụng các tính năng mới nhất như Coroutines, Flow).
- **Giao diện người dùng (UI)**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Declarative UI, giúp xây dựng giao diện nhanh chóng và linh hoạt).
- **Kiến trúc**: MVVM (Model-View-ViewModel) - Giúp phân tách logic và UI, dễ dàng cho việc bảo trì và mở rộng.
- **Lưu trữ cục bộ**: [Room Database](https://developer.android.com/training/data-storage/room) (Một lớp trừu tượng trên SQLite để quản lý dữ liệu hiệu quả).
- **Navigation**: [Navigation Compose](https://developer.android.com/jetpack/compose/navigation) (Quản lý luồng di chuyển giữa các màn hình).

## 5. Hướng phát triển

Salud không chỉ dừng lại ở các tính năng hiện tại mà còn có tiềm năng mở rộng trong tương lai:
- **Tích hợp Smartwatch/Google Fit**: Đồng bộ dữ liệu vận động và nhịp tim tự động.
- **Gợi ý thông minh (AI)**: Dùng AI để phân tích dữ liệu và đưa ra gợi ý về dinh dưỡng, luyện tập phù hợp với từng cá nhân.
- **Cộng đồng**: Xây dựng một mạng xã hội nhỏ để người dùng có thể chia sẻ thành tích, công thức nấu ăn và tạo động lực cho nhau.
- **Quét mã vạch sản phẩm**: Nhanh chóng lấy thông tin dinh dưỡng của thực phẩm.

