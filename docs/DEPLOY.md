# GCP e2-micro에 잼얘 백엔드 올리기

대상 주소는 `https://jamye-backend.o-r.kr` 이다. 내도메인 무료 서브도메인이고, A 레코드가 VM 고정 IP를 가리킨다. 프론트 `https://jamye.p-e.kr` 는 S3 + CloudFront이고 저장소는 `jamye-frontend` 다. Cordova 앱(Origin `file://`, `https://localhost`)용 CORS는 그대로 둔다. 이미지 저장은 기존 S3를 계속 쓴다.

예전에는 AWS(EC2, RDS, ElastiCache, Elastic Beanstalk)에 올려 두었다. 비용 때문에 내려왔고, 지금은 Google Cloud VM 한 대다.

구성은 Compute Engine `e2-micro` (`us-central1`, 무료 등급, Ubuntu 22.04) 위의 Docker Compose다. RAM은 1GB이고 스왑 3GB를 붙여 둔다. 앱이 뜨기까지 약 6~7분 걸린다.

- `app`: Spring Boot 3.3 / Kotlin / Java 21. 이미지는 `jamye-backend:amd64`
- `mysql`: MySQL 8.0. 호스트 포트로 열지 않는다
- `redis`: Redis 7. 호스트 포트로 열지 않는다
- `caddy`: 80/443, Let's Encrypt 인증서, `/ws` WebSocket 프록시

`docker-compose.prod.yml`에 `docker-compose.gcp.yml`을 겹친다. gcp 파일은 저메모리 오버라이드다.

- 앱 `mem_limit` 640m. JVM `-Xms128m -Xmx300m -Xss512k -XX:MaxMetaspaceSize=160m -XX:ReservedCodeCacheSize=48m -XX:+UseSerialGC -XX:TieredStopAtLevel=1`
- MySQL `mem_limit` 400m. `--innodb-buffer-pool-size=64M`, `--max-connections=30`
- Redis `mem_limit` 64m. `maxmemory 32mb`
- Caddy `mem_limit` 64m

VM 위에서는 빌드하지 않는다. 1GB에서는 Gradle이 버티기 어렵다.

## 1. Mac에서 이미지 만들기

```bash
docker buildx build --platform linux/amd64 -t jamye-backend:amd64 .
docker save jamye-backend:amd64 | gzip > jamye-backend-amd64.tar.gz
gcloud compute scp jamye-backend-amd64.tar.gz <인스턴스>:~ --zone=<us-central1 존>
```

`<인스턴스>`와 존만 실제 값으로 바꾼다. IP는 문서에 적지 않는다.

## 2. VM에서 기동

```bash
docker load < jamye-backend-amd64.tar.gz
sudo docker compose -f docker-compose.prod.yml -f docker-compose.gcp.yml up -d --no-build
sudo docker compose -f docker-compose.prod.yml -f docker-compose.gcp.yml ps
```

작업 디렉터리는 compose 파일, `.env`, `secrets/`, `Caddyfile`이 있는 곳이다. `--no-build`를 빼면 VM에서 다시 빌드하려 한다.

첫 기동은 6~7분 걸린다. 로그로 본다.

```bash
sudo docker compose -f docker-compose.prod.yml -f docker-compose.gcp.yml logs -f app
curl -fsS https://jamye-backend.o-r.kr/
```

인증서가 안 나오면 `logs caddy`에서 ACME 오류를 본다. DNS가 VM을 가리키는지, 80/443이 열려 있는지를 먼저 확인한다. 3306과 6379는 열지 않는다.

## 3. 설정

`.env`와 `secrets/`는 git에 올리지 않는다. `.env`는 `.env.example`을 복사해 값을 채운다. 비밀번호에 `$`를 넣지 않는다. Compose가 변수를 치환한다.

```bash
cp .env.example .env
mkdir -p secrets
```

시크릿 파일:

- `secrets/firebase-adminsdk.json` (`FIREBASE_CREDENTIALS_FILE`)
- `secrets/google-vision.json` (`GOOGLE_CREDENTIALS_FILE`)

`.env`에 넣는 이름만 적는다. 값은 적지 않는다.

- `DOMAIN`, `ACME_EMAIL`, `FRONT_URL`, `IMAGE_URL`, `CORS_ALLOWED_ORIGINS`
- `SPRING_PROFILES_ACTIVE`, `JPA_DDL_AUTO`
- `MYSQL_ROOT_PASSWORD`, `MYSQL_DATABASE`, `MYSQL_USER`, `MYSQL_PASSWORD`
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`
- `JWT_SECRET_KEY`
- `DISCORD_CLIENT_ID`, `DISCORD_SECRET_KEY`, `BOT_TOKEN`, `DISCORD_REDIRECT`
- `KAKAO_CLIENT_ID`, `KAKAO_REDIRECT`
- `GOOGLE_CLIENT_ID`
- `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_S3_BUCKET`, `AWS_REGION`
- `CLOUD_AWS_CREDENTIALS_ACCESSKEY`, `CLOUD_AWS_CREDENTIALS_SECRETKEY`
- `FIREBASE_CREDENTIALS_FILE`, `FIREBASE_PROJECT_ID`
- `GOOGLE_CREDENTIALS_FILE`
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`

`CLOUD_AWS_CREDENTIALS_ACCESSKEY`와 `CLOUD_AWS_CREDENTIALS_SECRETKEY`는 예전 이름이다. `.env.example`에는 없어도, S3를 아직 쓰므로 `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY`와 같이 넣는다.

`DB_URL`의 데이터베이스 이름은 `MYSQL_DATABASE`와 같아야 한다. `JWT_SECRET_KEY`를 바꾸면 기존 로그인 토큰이 모두 무효가 된다. `SENTRY_DSN`은 비우면 전송하지 않는다. 운영 데이터가 있으면 `JPA_DDL_AUTO`는 `none`으로 둔다.

`chmod 644 secrets/*.json` 정도로 컨테이너가 읽을 수 있게 한다.

## 4. 자주 보는 명령

```bash
sudo docker compose -f docker-compose.prod.yml -f docker-compose.gcp.yml logs -f app
sudo docker compose -f docker-compose.prod.yml -f docker-compose.gcp.yml restart app
sudo docker compose -f docker-compose.prod.yml -f docker-compose.gcp.yml up -d --no-build
```

백업은 MySQL 볼륨을 덤프한다. 비밀번호는 `.env`의 `MYSQL_ROOT_PASSWORD`다.

```bash
sudo docker compose -f docker-compose.prod.yml -f docker-compose.gcp.yml exec mysql \
  mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --single-transaction --default-character-set=utf8mb4 \
  "$MYSQL_DATABASE" > "jamye-$(date +%F).sql"
```

## 5. 로컬에서 직접 실행

로컬 Compose는 `docker-compose.yaml`이다. MySQL과 Redis를 `127.0.0.1`에만 연다. IDE에서 실행할 때는 `SPRING_PROFILES_ACTIVE=local`과 `DB_URL`을 `localhost`로 맞춘다. `.env` 예시는 같은 `.env.example`을 본다.
