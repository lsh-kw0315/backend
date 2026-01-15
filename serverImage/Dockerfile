# 베이스 이미지 설정 (OpenJDK 사용)
FROM openjdk:21-jdk-slim

# Java 버전 확인 (확인 후 적절한 버전 설치)
RUN java -version

# 시스템 시간대 설정
RUN ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone

# 작업 디렉터리 설정
WORKDIR /app

# JAR 파일 복사
COPY build/libs/server-0.0.1-SNAPSHOT.jar app.jar

# 환경 변수 설정 (Spring 프로파일 & 시간대)
ENV SPRING_PROFILES_ACTIVE=prod
ENV TZ=Asia/Seoul

# 실행 명령어
CMD ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
