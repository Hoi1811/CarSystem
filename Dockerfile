# ---- Giai đoạn 1: Build ----
# Dùng image maven để build code, không quan tâm đến application.yml ở đây
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
# Lệnh build sẽ đọc application.yml (bản an toàn) và đóng gói vào JAR
RUN mvn package -DskipTests

# ---- Giai đoạn 2: Serve ----
# Dùng image JRE gọn nhẹ
FROM openjdk:23-jdk-slim-bookworm
WORKDIR /app
# Chỉ copy file app.jar (đã chứa application.yml an toàn bên trong)
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
# Lệnh chạy ứng dụng. Các biến môi trường sẽ được  Docker Compose tiêm vào lúc chạy.
ENTRYPOINT ["java", "-jar", "app.jar"]