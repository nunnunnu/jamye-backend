## ✨ 잼얘가챠 - Backend

프론트 주소: https://jamye.p-e.kr

백엔드 주소: https://jamye-backend.o-r.kr

잼얘가챠는 친구들과의 추억을 메시지나 게시글 형식으로 저장하고,

이를 가챠처럼 랜덤으로 뽑아보며 다시 떠올릴 수 있는 서비스입니다. 

---

### 📌 Language

- Kotlin


### 📦 Library & Framework

- Spring Boot

- Spring Security

- JPA (Hibernate)

- Redis

- Quartz Scheduler

- Firebase Admin SDK (FCM) - 모바일 알람

- Discord Bot API

- Google Cloud Vision API (OCR)

- JavaMail (SMTP)

- Sentry

- Logback


### 🗃 Database

- MySQL (AWS RDS)

- Redis (블랙리스트 토큰 및 그룹 투표정보/뽑기횟수 등 정보 관리)


### ⚙ ORM

- Spring Data JPA


### 🚀 Deploy

- EC2 (Backend 서버)

- Jenkins (별도 EC2에서 Webhook 기반 자동 배포)

- Docker Compose (로컬 개발 환경에만 적용하였습니다)

- Nginx (무중단 배포)

- HTTPS 인증 (도메인 보안)

- AWS S3 (이미지 저장소)


---

### 🧩 주요 기능

#### ✅ 그룹

- 그룹 생성 / 탈퇴 (마스터 탈퇴 시 권한 자동 양도)

- 초대코드 발급 및 초대코드로 가입

- 그룹 목록 조회, 삭제(투표 기반)

- 삭제 투표 및 과반수 동의 시 스케쥴러를 통한 그룹 일괄 삭제

- 그룹 소속 회원 목록 및 정보 수정


#### 📝 게시글

- 게시글 작성 (알림 전송)

    - 일반 텍스트 타입

    - 메시지 타입 (카톡 대화 이미지 → 자동 메시지 변환)

- 게시글 조회 및 필터링/페이징 목록

- 태그 관리

- 게시글 뽑기 (1일 3회 제한)

- 게시글 수정/삭제 (알림 전송 포함)


#### 💬 댓글

- 댓글 작성/조회/수정/삭제 (작성자 알림 전송)


#### 📬 쪽지함

- 쪽지 확인/삭제

- Socket을 통한 실시간 알림 수신

- 모든 알림 읽음 처리 및 자동 삭제 (30일 지난 알림)

- Discord 봇 알림 연동


#### 👤 회원

- 회원가입 / 로그인 / 로그아웃

- 회원 정보 조회 및 수정

- 회원 탈퇴

- 아이디/비밀번호 찾기 (이메일 인증 기반)
# ERD
- 회원
<img width="1211" alt="IMG-20250504151116" src="https://github.com/user-attachments/assets/98c26de9-3096-4901-b347-7ad0b5b8d172" />
    
- 그룹
<img width="1224" alt="IMG-20250504151155" src="https://github.com/user-attachments/assets/1c967613-eac0-4801-b371-fad52c586e80" />

- 게시글
<img width="1457" alt="IMG-20250504151215" src="https://github.com/user-attachments/assets/289d1e01-422e-4a75-b01a-9b117a99d52c" />

# 실제 화면


# 아키텍쳐 다이어그램
<img width="1367" alt="image" src="https://github.com/user-attachments/assets/8c95ddba-c7b9-4706-a232-d6485f7c66ed" />

