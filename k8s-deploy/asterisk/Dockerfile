FROM andrius/asterisk:latest

# 💡 [핵심] 리얼타임에 필수적인 MariaDB/MySQL ODBC 드라이버를 빌드 시 강제 설치 (Debian 계열 대응)
RUN apt-get update && apt-get install -y \
    odbc-mariadb \
    unixodbc \
    && rm -rf /var/lib/apt/lists/*

# 필요한 설정 파일 매핑을 위해 노출
EXPOSE 5038 5060/udp 5060/tcp 10000-10050/udp

CMD ["/usr/sbin/asterisk", "-f"]
