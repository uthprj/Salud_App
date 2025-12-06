# Salud - Ứng dụng Quản lý sức khỏe 🥗🏃‍♂️💤

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

Salud (tiếng Tây Ban Nha có nghĩa là "sức khỏe") là ứng dụng di động Android toàn diện, giúp người dùng theo dõi, quản lý và cải thiện sức khỏe thông qua việc giám sát các chỉ số cơ thể, dinh dưỡng, luyện tập và giấc ngủ. Được xây dựng với công nghệ hiện đại như Kotlin, Jetpack Compose và Firebase, kết hợp AI (Google Gemini) để tư vấn thông minh.

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
| ❤️ **Theo dõi Sức khỏe**   | Ghi nhận và trực quan hóa các chỉ số (cân nặng, huyết áp, nhịp tim) qua biểu đồ theo thời gian. | ✅ Hoàn thành |
| 🥗 **Quản lý Dinh dưỡng**  | Theo dõi lượng calo nạp vào từ các bữa ăn, tìm kiếm thực phẩm và xây dựng thực đơn.              | ✅ Hoàn thành |
| 🏃‍♂️ **Quản lý Vận động**    | Ghi nhận các hoạt động thể chất, theo dõi thời lượng và lượng calo tiêu thụ.                   | ✅ Hoàn thành   |
| 💤 **Theo dõi Giấc ngủ**    | Ghi nhận thời gian ngủ và thức dậy, đánh giá chất lượng giấc ngủ.                               | ✅ Hoàn thành   |
| 🎯 **Thiết lập Mục tiêu**   | Đặt ra các mục tiêu sức khỏe (ví dụ: giảm 5kg) và theo dõi tiến trình thực hiện.                | ✅ Hoàn thành   |
| 🎯 **AI hỗ trợ**   | AI giúp báo cáo tổng quan về sức khỏe, gợi ý bữa ăn và tập phù hợp với tình trạng, mục tiêu                | ✅ Hoàn thành   |


## 4. Công nghệ sử dụng

Dự án được xây dựng hoàn toàn bằng các công nghệ hiện đại và phổ biến trong hệ sinh thái Android.

- **Ngôn ngữ**: [Kotlin](https://kotlinlang.org/) (Sử dụng các tính năng mới nhất như Coroutines, Flow).
- **Giao diện người dùng (UI)**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Declarative UI, giúp xây dựng giao diện nhanh chóng và linh hoạt).
- **Kiến trúc**: MVVM (Model-View-ViewModel) - Giúp phân tách logic và UI, dễ dàng cho việc bảo trì và mở rộng.
- **Navigation**: [Navigation Compose](https://developer.android.com/jetpack/compose/navigation) (Quản lý luồng di chuyển giữa các màn hình).

## 5. Hướng phát triển

Salud không chỉ dừng lại ở các tính năng hiện tại mà còn có tiềm năng mở rộng trong tương lai:
- **Tích hợp Smartwatch/Google Fit**: Đồng bộ dữ liệu vận động và nhịp tim tự độngjso
- **Kết nối với bác sĩ**: Kết nối với bác sĩ để được tư vấn trực tiếp

