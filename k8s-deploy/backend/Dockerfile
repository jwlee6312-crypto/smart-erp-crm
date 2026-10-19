FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# 윈도우(빌드 서버)에서 이미 만들어진 JAR 파일만 복사 (초고속)
COPY erp-backend/build/libs/*-SNAPSHOT.jar app.jar
EXPOSE 8080
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
ENTRYPOINT ["java", "-Xmx512m", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
