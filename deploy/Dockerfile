# 빌드 단계
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY . .
RUN ./gradlew build -x test

# 실행 단계
FROM eclipse-temurin:21-jre-alpine AS runtime
RUN apk add --no-cache font-noto font-noto-cjk ttf-dejavu
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]