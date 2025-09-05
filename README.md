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
Kết quả JUnit Test
<img width="543" height="142" alt="image" src="https://github.com/user-attachments/assets/0d24a9dd-d83a-4496-982f-efd95c659cc4" />

Output từ mô phỏng trận đấu
