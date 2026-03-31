# ---- Giai đoạn 1: Build ----
# Dùng image Maven + JDK 21 để build code
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
# Lệnh build sẽ đọc application.yml và đóng gói vào JAR
RUN mvn package -DskipTests

# ---- Giai đoạn 2: Serve ----
# Dùng image JRE 21 gọn nhẹ (thống nhất version với giai đoạn build)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Chỉ copy file app.jar đã build
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
# Lệnh chạy ứng dụng. Các biến môi trường sẽ được Docker Compose tiêm vào lúc chạy.
ENTRYPOINT ["java", "-jar", "app.jar"]