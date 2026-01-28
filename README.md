# VietBank Backend System:
VietBank là một dự án **backend ngân hàng giả lập** được xây dựng bằng **Java Spring Boot**, mô phỏng các chức năng cốt lõi của một hệ thống ngân hàng hiện đại như xác thực người dùng, phân quyền, quản lý dữ liệu và bảo mật API.
Dự án được phát triển với mục tiêu học tập và thực hành **Java Backend**
---
##  Tính năng chính:
- Xây dựng **RESTful API** cho hệ thống ngân hàng
- **Xác thực người dùng bằng JWT (JSON Web Token)**
- **Phân quyền (Role-based Authorization)** với Spring Security
- **Validate dữ liệu đầu vào** bằng Bean Validation
- Hỗ trợ **đa ngôn ngữ (Internationalization – i18n)** cho message và response
- Tích hợp **MySQL** với **JPA/Hibernate**
- Sử dụng **Lombok** để giảm boilerplate code
- **Docker hóa ứng dụng**, chạy độc lập bằng `Dockerfile` và `docker-compose`
---
##  Kiến trúc dự án:
Dự án được tổ chức theo mô hình **Layered Architecture**:
- **Controller**: Xử lý request/response từ client
- **Service**: Chứa business logic
- **Repository**: Tương tác với database thông qua JPA/Hibernate
- **Security**: Cấu hình JWT, authentication và authorization
- **Config**: Cấu hình i18n, security, các bean dùng chung
---
## Công nghệ sử dụng:
Java, Spring Boot, Spring Security,Spring Data JPA, JWT, RESTful API, MySQL, Hibernate, Bean Validation, Internationalization (i18n), Lombok, Docker, Docker Compose
---
## Yêu cầu phần mềm:
- MySQL Workbench 
- Postman ( API Dog )
- Docker Desktop
---
## Cách chạy dự án bằng Docker:
## 1. Clone repository
git clone https://github.com/hieuhoadang/vietbank.git
cd vietbank
## 2. Chạy ứng dụng bằng Docker Compose
docker-compose up -d --build
## 3. import file script sql vào MySQL
