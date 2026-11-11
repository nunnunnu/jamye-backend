# Oracle Cloud Always Free에 잼얘 백엔드 올리기

대상 주소는 `https://jamye-backend.o-r.kr` 이다. 프론트 `https://jamye.p-e.kr` 와 Cordova 앱(Origin `file://`, `https://localhost`)은 그대로 둔다. 이미지 저장은 기존 S3 버킷 `jamye-image`와 CloudFront `https://d23eha8a4ssn72.cloudfront.net` 를 계속 쓴다.

구성은 VM 한 대 위의 Docker Compose다.

- `app`: Spring Boot (JRE 21, ARM64/AMD64)
- `mysql`: MySQL 8.0. 호스트 포트로 열지 않는다
- `redis`: Redis 7. 호스트 포트로 열지 않는다
- `caddy`: 80/443, Let's Encrypt 인증서, `/ws` WebSocket 프록시

기본 메모리 한도는 앱 768MB, MySQL 640MB, Redis 160MB, Caddy 96MB다. JVM 힙은 컨테이너 메모리의 55%만 쓴다. 4GB RAM VM에 맞춘 값이다.

## 1. VM 만들기

OCI 콘솔에서 Compute 인스턴스를 만든다.

- Image: Canonical Ubuntu 22.04 또는 24.04 **aarch64**
- Shape: `VM.Standard.A1.Flex` (Always Free Ampere)
- 여유 쿼터가 있으면 2 OCPU / 8GB. 최소로 쓸 거면 1 OCPU / 4GB
- Boot volume 50GB 이상. Always Free 블록 볼륨 한도 안에서 고른다
- 공인 IPv4를 붙인다
- SSH 키를 등록한다

이 VM은 x86 EC2용 이미지를 쓰면 안 된다. 공식 `eclipse-temurin`과 `mysql`, `redis`, `caddy` 이미지는 arm64를 제공한다. `docker compose build`는 VM 위에서 실행한다.

메모리가 4GB 이하면 첫 Gradle 빌드 전에 스왑을 단다.

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

RAM이 12GB 이상이면 `docker-compose.prod.yml`의 `mem_limit`을 앱 1280m, MySQL 1g 정도로 올리고, `.env`의 `JAVA_OPTS` 주석을 해제해도 된다. `innodb-buffer-pool-size`는 MySQL 한도의 절반 이하로 맞춘다.

## 2. 80/443 열기

OCI 보안 목록(Security List) 또는 인스턴스에 붙은 Network Security Group **둘 중 실제로 쓰는 쪽**에 인그레스를 추가한다.

- TCP 80, 소스 `0.0.0.0/0`
- TCP 443, 소스 `0.0.0.0/0`
- TCP 22는 본인 IP만 허용하는 편이 낫다

3306과 6379는 열지 않는다. Compose도 이 포트를 호스트에 바인딩하지 않는다.

Oracle Ubuntu 이미지는 OS 방화벽으로 iptables를 쓰고, 기본 정책이 22번 외의 신규 연결을 REJECT한다. 보안 목록만 열면 Let's Encrypt와 사이트 접속이 타임아웃된다.

```bash
sudo iptables -L INPUT -n --line-numbers
```

`REJECT` 줄 번호를 확인한 다음, 그 줄에 INSERT해서 REJECT보다 앞에 둔다. 아래에서 `6`은 예시이니 출력된 번호로 바꾼다.

```bash
sudo iptables -I INPUT 6 -p tcp -m conntrack --ctstate NEW -m tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT 6 -p tcp -m conntrack --ctstate NEW -m tcp --dport 443 -j ACCEPT
sudo iptables -L INPUT -n --line-numbers
```

재부팅 후에도 남기려면:

```bash
sudo apt-get update
sudo apt-get install -y iptables-persistent
sudo netfilter-persistent save
```

`sudo ufw status`가 `active`이면 `sudo ufw allow 80/tcp`와 `sudo ufw allow 443/tcp`도 한다. OCI Ubuntu에서는 보통 ufw가 꺼져 있고 iptables가 막는다. IPv6를 쓰면 `ip6tables`에도 같은 규칙을 넣는다.

## 3. DNS (내도메인.한국)

내도메인.한국에서 `jamye-backend.o-r.kr`의 A 레코드를 기존 EC2 IP에서 이 VM의 공인 IPv4로 바꾼다. 이전 A 레코드는 지운다. 바꾸기 전에 TTL을 짧게 내려 두면 전파가 빠르다.

Caddy는 HTTP-01로 인증서를 발급한다. 레코드는 프록시 없이 VM IP를 직접 가리켜야 하고, 80번 포트가 인터넷에서 열려 있어야 한다.

## 4. Docker와 앱 기동

```bash
sudo apt-get update
sudo apt-get install -y docker.io docker-compose-v2 git
sudo usermod -aG docker "$USER"
```

그룹 적용을 위해 SSH를 다시 접속한다.

```bash
sudo mkdir -p /opt/jamye
sudo chown "$USER":"$USER" /opt/jamye
cd /opt/jamye
git clone https://github.com/nunnunnu/jamye-backend.git .
cp .env.example .env
mkdir -p secrets
```

`.env`의 `change-me` / `replace-me`를 채운다. 비밀번호에는 `$`를 넣지 않는다.

- `JWT_SECRET_KEY`는 `openssl rand -base64 32`로 만든다. 예전에 쓰던 키를 그대로 넣으면 기존 로그인 세션이 유지된다. 새 키로 바꾸면 전 사용자가 다시 로그인해야 한다.
- Gmail 앱 비밀번호, Discord, Kakao, Google, AWS, Sentry는 아래 "돌려야 하는 비밀"을 먼저 처리하고 새 값을 넣는다.
- `DB_URL`의 데이터베이스 이름은 `MYSQL_DATABASE`와 같아야 한다. 기본값은 기존 RDS와 같은 `jamye_prod`다.
- Firebase 서비스 계정 JSON을 `secrets/firebase-adminsdk.json`에 둔다.
- Vision용 GCP 서비스 계정 JSON을 `secrets/google-vision.json`에 둔다.
- `FIREBASE_PROJECT_ID`는 Firebase 프로젝트 ID다.

```bash
chmod 644 secrets/*.json
docker compose -f docker-compose.prod.yml up -d --build
docker compose -f docker-compose.prod.yml ps
```

첫 빌드는 JDK 이미지 안에서 Gradle이 의존성을 받기 때문에 수 분 걸린다. 확인:

```bash
docker compose -f docker-compose.prod.yml logs -f app
curl -fsS https://jamye-backend.o-r.kr/
```

`/`는 `build test3` 문자열을 돌려준다. 브라우저에서 프론트 `https://jamye.p-e.kr` 로그인, 그리고 알림 소켓(`/ws`, SockJS/STOMP)을 한 번 확인한다.

인증서 발급이 실패하면 `docker compose -f docker-compose.prod.yml logs caddy`에서 ACME 오류를 본다. 대부분 DNS가 아직 안 바뀌었거나, 보안 목록 또는 iptables에서 80이 닫힌 경우다.

## 5. RDS에서 데이터 옮기기

RDS를 이미 종료했다면, 종료 전에 떠 둔 `mysqldump` 또는 스냅샷이 있어야 한다. 스냅샷만 있으면 임시로 복원해 덤프한 뒤 다시 지운다.

덤프를 받을 수 있는 머신에서:

```bash
mysqldump -h jamye-service.cfek882iekrl.ap-northeast-2.rds.amazonaws.com \
  -u jm -p \
  --single-transaction --routines --triggers --set-gtid-purged=OFF \
  --default-character-set=utf8mb4 \
  jamye_prod > jamye_prod.sql
```

DEFINER 구문이 있으면 로컬 MySQL 사용자가 달라 임포트가 실패한다. 미리 지운다.

```bash
sed -i -E 's/DEFINER=`[^`]+`@`[^`]+`//g' jamye_prod.sql
```

파일을 VM의 `/opt/jamye`로 복사한 뒤, 앱보다 MySQL을 먼저 띄우고 넣는다. `.env`를 현재 셸에 읽힌 다음 실행한다.

```bash
set -a
. ./.env
set +a

docker compose -f docker-compose.prod.yml up -d mysql
docker compose -f docker-compose.prod.yml exec -T mysql \
  mysql -uroot -p"$MYSQL_ROOT_PASSWORD" --default-character-set=utf8mb4 \
  "$MYSQL_DATABASE" < jamye_prod.sql
```

덤프 안에 `CREATE DATABASE` / `USE jamye_prod`가 있으면 그 스키마로 들어간다. `MYSQL_DATABASE`와 `DB_URL`도 `jamye_prod`로 맞춘다.

`GTID_PURGED` 오류가 나면 해당 `SET` 줄을 지우고 다시 넣는다. 빈 데이터베이스에 스키마 덤프가 전혀 없을 때만 `.env`의 `JPA_DDL_AUTO=update`로 한 번 기동하고, 테이블이 생긴 뒤 `none`으로 되돌린다. 운영 데이터가 있으면 `update`로 올리지 않는다.

앱을 이미 띄웠다면 임포트 후 재시작한다.

```bash
docker compose -f docker-compose.prod.yml up -d
```

## 6. 선택: GitHub Actions

예전 워크플로는 `ubuntu-latest`에서 jar를 만들고, self-hosted 러너에서 `~/app/jamye/start-port.sh`로 무중단 배포했다. 그 스크립트는 이 저장소에 없고, 러너가 있던 EC2는 종료된 상태다. 매 푸시마다 죽은 러너를 기다리지 않도록 배포 잡은 수동 실행만 하게 바꿔 두었다.

푸시와 PR은 GitHub 호스트 러너에서 `./gradlew bootJar -x test`만 한다. 테스트는 MySQL, Redis, Firebase, S3가 필요해서 CI에서 건너뛴다.

Oracle VM에 배포 버튼을 쓰려면:

1. GitHub 저장소 Settings → Actions → Runners → New self-hosted runner. OS는 Linux ARM64.
2. 러너를 VM에 설치한다. 서비스 계정이 `docker` 그룹이어야 한다.
3. 코드는 `/opt/jamye`에 둔다. `.env`와 `secrets/`는 이 디렉터리에만 있고 git에 없다.
4. Actions 탭에서 **CI** 워크플로를 `workflow_dispatch`로 실행한다.

배포 잡은 `actions/checkout`을 쓰지 않는다. checkout의 clean이 `.env`와 서비스 계정 JSON을 지우기 때문이다. 대신 `/opt/jamye`에서 `git pull` 한 뒤 `docker compose -f docker-compose.prod.yml up -d --build`를 실행한다.

러너 없이 배포할 때는 VM에서 같은 compose 명령을 직접 실행하면 된다.

## 7. 돌려야 하는 비밀

이 저장소에 비밀번호가 커밋되어 있었다. git history는 고치지 않았으므로 예전 커밋을 보면 값이 그대로 있다. 아래는 폐기하고 새로 발급한다.

- Google 계정 `nod15277@gmail.com`의 Gmail 앱 비밀번호. Google 계정 보안에서 해당 앱 비밀번호를 제거한 뒤 새로 만들어 `MAIL_PASSWORD`에 넣는다.
- RDS MySQL 사용자 `jm`, 데이터베이스 `jamye_prod` (인스턴스 `jamye-service`, 리전 ap-northeast-2). 인스턴스를 이미 지웠으면 그 비밀번호는 더 이상 접속에 쓰이지 않는다. 스냅샷을 다시 살릴 계획이면 복원 직후 비밀번호를 바꾼다. 새 VM의 `MYSQL_PASSWORD`는 그 값과 다르게 만든다.
- Sentry DSN (`o4507389505699840` / 프로젝트 `4508822101819392`). Sentry에서 해당 DSN을 지우고 새 DSN을 만들어 `SENTRY_DSN`에 넣는다. 에러 수집이 필요 없으면 비워 둔다.

AWS 액세스 키, Discord 시크릿, Kakao, Google OAuth, JWT, Firebase/GCP JSON은 이 커밋 시점의 파일 내용에는 없었다. 다만 예전에 채팅이나 다른 사본으로 새어 나갔다면 각 콘솔에서 키를 재발급한다. Firebase JSON과 Vision JSON은 `secrets/`에만 둔다.

## 8. 자주 보는 명령

```bash
docker compose -f docker-compose.prod.yml logs -f app
docker compose -f docker-compose.prod.yml restart app
docker compose -f docker-compose.prod.yml up -d --build
```

백업은 MySQL 볼륨을 덤프한다.

```bash
docker compose -f docker-compose.prod.yml exec mysql \
  mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --single-transaction --default-character-set=utf8mb4 \
  "$MYSQL_DATABASE" > "jamye-$(date +%F).sql"
```

## 9. 로컬에서 직접 실행

운영 Compose와 같은 `.env`를 쓸 수 있다. IDE에서 실행할 때는 `SPRING_PROFILES_ACTIVE=local`과 `DB_URL`을 `localhost`로 맞춘다. 로컬 Compose는 `docker-compose.yaml`이며 MySQL과 Redis를 `127.0.0.1`에만 연다.
