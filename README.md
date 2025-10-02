# Đề tài: Salud - Ứng dụng Quản lý sức khỏe

## 1. Lý do chọn đề tài
Trong xã hội hiện đại, việc chăm sóc sức khỏe ngày càng được quan tâm. Tuy nhiên, nhiều người gặp khó khăn trong việc theo dõi và duy trì lối sống lành mạnh do bận rộn, thiếu kiến thức hoặc không có công cụ hỗ trợ hiệu quả.  
Ứng dụng **Salud** ra đời nhằm giúp người dùng quản lý toàn diện sức khỏe bản thân, từ dinh dưỡng, vận động đến giấc ngủ, góp phần xây dựng lối sống cân bằng và khoa học.

## 2. Ý tưởng ứng dụng
Salud được xây dựng như một **trợ lý sức khỏe cá nhân**, giúp người dùng:
- Theo dõi chỉ số cơ thể và tình trạng sức khỏe.
- Quản lý chế độ ăn uống, tính toán calo.
- Lập lịch tập luyện phù hợp với mục tiêu.
- Theo dõi chất lượng giấc ngủ.
- Đặt mục tiêu và quản lý tiến trình đạt được.
  

Ứng dụng vừa hỗ trợ **sinh viên, người đi làm bận rộn** vừa phù hợp với **người dùng quan tâm đến sức khỏe lâu dài**.

## 3. Nghiên cứu và phân tích

### 3.1. Phân tích nhu cầu
- Người dùng muốn **công cụ đơn giản, dễ sử dụng** để ghi nhận tình trạng sức khỏe hằng ngày.  
- Người bận rộn cần **báo cáo tổng quan (dashboard)** nhanh chóng.  
- Sinh viên, nhân viên văn phòng muốn có **gợi ý dinh dưỡng và luyện tập** phù hợp.  

### 3.2. Khảo sát các ứng dụng tương tự
- **Google Fit, Samsung Health**: theo dõi vận động, nhịp tim, nhưng thiếu phần quản lý dinh dưỡng chi tiết.  
- **MyFitnessPal**: mạnh về dinh dưỡng nhưng chưa đồng bộ tốt giấc ngủ và sức khỏe tổng quan.  
- **Salud** sẽ kết hợp các yếu tố còn thiếu và đưa ra **trải nghiệm toàn diện**.

### 3.3. Đối tượng người dùng
- Sinh viên, nhân viên văn phòng.
- Người muốn giảm cân/tăng cân, điều chỉnh chế độ ăn.  
- Người muốn theo dõi sức khỏe định kỳ để phòng bệnh.  

### 3.4. Tính năng chính
1. **Đăng nhập/Đăng ký**  
   - Xác thực người dùng an toàn (JWT/Session, mã hóa mật khẩu).  
2. **Dashboard**  
   - Hiển thị tổng quan tình trạng sức khỏe (cân nặng, BMI, huyết áp, nhịp tim).  
3. **Theo dõi sức khỏe**  
   - Ghi nhận cân nặng, chỉ số BMI, huyết áp, nhịp tim.  
   - Biểu đồ theo dõi theo thời gian.  
4. **Quản lý dinh dưỡng**  
   - Theo dõi lượng calo nạp vào.  
   - Quản lý bữa ăn hằng ngày.  
   - Gợi ý chế độ ăn theo mục tiêu.  
5. **Lịch trình luyện tập**  
   - Ghi nhận các buổi tập.  
   - Theo dõi thời lượng và loại hình tập luyện.  
6. **Quản lý giấc ngủ**  
   - Ghi nhận giờ ngủ, giờ thức.  
   - Theo dõi chất lượng giấc ngủ.  
7. **Hồ sơ cá nhân**  
   - Quản lý thông tin cá nhân.  
   - Đặt mục tiêu (giảm cân, tăng cơ, cải thiện sức khỏe).  

### 3.5. Kiến trúc và công nghệ đề xuất
- **Frontend**: React Native / Kotlin (Android) / Swift (iOS).  
- **Backend**: Node.js hoặc PHP (REST API).  
- **Cơ sở dữ liệu**: MySQL hoặc MongoDB.  
- **Realtime/Đồng bộ**: Firebase/Socket.IO (nếu cần).  
- **Bảo mật**: Mã hóa dữ liệu, bảo vệ API bằng JWT.  

## 4. Kết luận
Salud là ứng dụng quản lý sức khỏe toàn diện, kết hợp **theo dõi sức khỏe, dinh dưỡng, luyện tập và giấc ngủ**.  
Ứng dụng hứa hẹn mang lại giá trị thực tiễn, dễ áp dụng cho sinh viên và người dùng bận rộn, đồng thời có tiềm năng phát triển lâu dài với các tính năng nâng cao như tích hợp smartwatch, AI gợi ý dinh dưỡng, và cộng đồng chia sẻ lối sống khỏe mạnh.
