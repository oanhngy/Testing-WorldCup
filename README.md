# Testing-WorldCup

## 1. GIỚI THIỆU
Dự án WorldCup phát triển bằng Java với mục tiêu mô phỏng và kiểm thử hệ thống quản lý giải bóng đá World Cup.
Dự án thực hiện **kiểm thử phần mềm** với số lượng lớn test case bao gồm **Unit Test, Integration Test và Automated Test**.

## 2. MAIN FUNCTION
- Quản lý thông tin đội bóng, cầu thủ, trận đấu, các bảng đấu và các vòng đấu loại trực tieếp
- Sinh dữ liệu mẫu (sample teams, fixtures) và tính toán kết quả
- Viết **200 Unit Test** cho các thành phần Player, Team, Match, Goal, Tournament...
- **Integration Test** để kiểm thử tương tác giữa các module
- Ứng dụng kiểm thử tự động bằng JUnit

## 3. CÔNG NGHỆ
- Ngôn ngữ: Java
- Framework Testing: JUnit
- Quản lý dự án: Maven (pom.xml)
- CSDL: SimpleDB mô phỏng dữ liệu

## 4. CẤU TRÚC DỰ ÁN
- src/main/java/com/worldcup/: Code chính (Player, Team, Match, Tournament, Services…) 
- src/test/java/com/worldcup/: Các test cases (Unit Test & Integration Test) 
- data/worldcup.db: File dữ liệu giả lập 
- pom.xml: Cấu hình Maven
  
## 5. CÀI ĐẶT + CHẠY DỰ ÁN
1. Clone project từ GitHub
2. Mở project bằng IntelliJ/VSCode
3. Chạy lệnh Maven để build: mvn clean install
4. Chạy các test với JUnit: mvn test

## 6. HÌNH MINH HỌA
Kết quả Unit Test

<img width="871" height="134" alt="Screenshot 2025-09-05 at 19 23 29" src="https://github.com/user-attachments/assets/8a0ab2bb-de66-4304-8de8-27cb6ab8d976" />



Kết quả Integration Test

<img width="708" height="222" alt="Screenshot 2025-09-05 at 19 23 46" src="https://github.com/user-attachments/assets/6d96eccb-87a2-4e2a-bad8-1db30a446e1a" />



Output từ mô phỏng trận đấu (chưa đầy đủ)

<img width="398" height="235" alt="Screenshot 2025-09-05 at 19 26 52" src="https://github.com/user-attachments/assets/2a64bdc1-aaeb-4c32-b112-d48687fe9d01" />

<img width="398" height="235" alt="Screenshot 2025-09-05 at 19 26 52" src="https://github.com/user-attachments/assets/fcefb9a9-23c9-4cbe-a953-9ded0ba2ae5e" />

<img width="333" height="413" alt="Screenshot 2025-09-05 at 19 27 43" src="https://github.com/user-attachments/assets/ac525891-a846-41a2-8bec-525dbb12127c" />

<img width="383" height="123" alt="Screenshot 2025-09-05 at 19 27 50" src="https://github.com/user-attachments/assets/4849c085-1dc2-4104-8eb3-85ba95f531f7" />
