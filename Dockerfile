# 1. 데비안 기반 PGroonga 공식 이미지 사용 (PostgreSQL 17)
FROM groonga/pgroonga:latest-debian-17

# 2. PostGIS 설치 (데비안은 의존성 충돌이 거의 없습니다)
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    postgresql-17-postgis-3 && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*