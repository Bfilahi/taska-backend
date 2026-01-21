# Kickscorner
### Overview
Taska is a modern web application built to help you organize, track, and complete tasks efficiently. Designed with simplicity in mind, it combines a clean interface with powerful features to make task management effortless.

This is a backend project built with Spring Boot to demonstrate RESTful API design, database interaction, authentication, and authorization.

#

### Main features
- User registration & login (JWT authentication)
- CRUD operations
- Global exception handling and validation

#

### Tech Stack
- Backend: Spring Boot 4.0.0, Spring Web, Spring Security, JWT , JPA (Hibernate)
- Database: PostgreSQL
- Restful API Documentation: Swagger UI
- Language: Java 21
- Cloud: Cloudinary

#

### ERD 
<img width="677" height="256" alt="taska_erd" src="https://github.com/user-attachments/assets/05ab3282-6dab-433d-beaf-b183075c5a55" />

### Getting Started
1. Clone the repository
   
   ```
   git clone https://github.com/Bfilahi/taska-backend.git
   ```

2. Database Setup

   * Create a PostgreSQL database.
   * Update application.properties:
     
     ```
      spring.datasource.url=jdbc:postgresql://localhost:5432/yourdatabase
      spring.datasource.username=yourusername
      spring.datasource.password=yourpassword
     ```

3. Run the application

   ```
   cd taska-backend
   mvn spring-boot:run
   ```

#

### Restful API Documentation
#### Swagger
Once the application is running, it will be available at: http://localhost:8080/swagger-ui/index.html

#

### Author & License
&copy; 2026 <strong> Bfilahi </strong>
This project is for interview and technical demonstration purposes only. </br>
Not intended for commercial use.
