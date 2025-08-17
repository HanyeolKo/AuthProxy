# 1. Java 이미지 선택: 최신 LTS slim-buster(용량 작고 성능 좋음)
FROM eclipse-temurin:17-jdk-jammy

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. jar 파일 복사 (build 후에 위치에 따라 경로 수정)
COPY build/libs/*.jar app.jar

# 4. 실행 포트 명시(옵션: 실제 앱 포트에 맞게)
EXPOSE 8080

# 5. 실행 명령
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]