# 1단계: 빌드 스테이지
FROM node:20-alpine AS build-stage
WORKDIR /app
# 의존성 설치
COPY package*.json ./
RUN npm install
# 소스 복사 및 빌드
COPY . .
RUN npm run build

# 2단계: 실행 스테이지 (Nginx)
FROM nginx:stable-alpine
# 빌드 스테이지에서 생성된 dist 폴더만 복사
COPY --from=build-stage /app/dist /usr/share/nginx/html

# Nginx 기본 설정 제거 (K8s ConfigMap 주입 설정 사용)
RUN rm -rf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
