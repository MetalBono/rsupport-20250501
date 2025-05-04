RSUPPORT 과제 (공지사항 관리 REST API)
=============================
다음의 요구사항에 맞는 Back-end Application 을 구현한다.

주제
-----------------------------
공지사항 관리 REST API 구현

## 기능 요구사항
* 공지사항 등록, 수정, 삭제, 조회 API를 구현한다. 
* 공지사항 등록시 입력 항목은 다음과 같다. 
  * 제목, 내용, 공지 시작일시, 공지 종료일시, 첨부파일 (여러개)
* 공지사항 조회시 응답은 다음과 같다. 
  * 목록: 제목, 첨부파일 유무, 등록일시, 조회수, 작성자 
  * 상세: 제목, 내용, 등록일시, 조회수, 작성자, 첨부파일 
* 공지사항 검색은 다음과 같다. 
  * 검색어: 제목 + 내용, 제목 
  * 검색기간: 등록일자

비기능 요구사항 및 평가 항목
-----------------------------
* REST API로 구현
* 개발 언어는 Java, Kotlin 중 익숙한 개발 언어로 한다.
* 웹 프레임 워크는 Spring Boot 을 사용한다.
* Persistence 프레임 워크는 Hibernate 사용시 가산점
* 데이터 베이스는 제약 없음
* 기능 및 제약사항에 대한 단위/통합테스트 작성
* 대용량 트래픽을 고려하여 구현할 것
* 핵심 문제해결 전략 및 실행 방법등을 README 파일에 명시

## 사용된 기술
* Kotlin 1.9 (Jdk 21)
* Spring Boot 3.4.3
* Gradle
* MySQL, H2 (RDBMS)
* Redis (NoSQL) with Redisson
* JPA, Hibernate, QueryDSL

## 어플리케이션 실행 방법
### 사전 세팅
* 본 어플리케이션은 MySQL 과 Redis 기반으로 동작하며, 이는 **docker-compose 를 실행할 수 있는 환경** 을 필수로 합니다.
* Docker Desktop 과 같은 docker 실행 환경을 설치한 후, 프로젝트 root 디렉토리에서 다음의 명령어를 실행합니다.
  * 상세 설정은 프로젝트 root 디렉토리의 docker-compose.yml 을 참고해주세요.
```shell
-- 전체 저장소 기동
docker-compose up -d

-- 전체 저장소 중단
docker-compose down

-- 전체 저장소를 중단 하고 초기화
docker-compose down -v
```

* 이후 Spring Boot Application 인 **com.rsupport.pretest.kyusubkim.KyusubkimApplication** 을 실행해주시면 됩니다.
  * 별도로 필요한 실행 옵션은 없습니다.

### API 테스트 방법
* Swagger UI 를 이용하여 테스트할 수 있으며 경로는 아래와 같습니다.
  * http://localhost:8080/swagger-ui/index.html

#### 참고 사항
* 공지사항 등록 및 수정에 사용되는 첨부파일 정보 (attachements) 에 전달받은 filePath 는 아래의 API 로 사전 업로드 후 응답으로 제공되는 값을 사용하는 것으로 가정합니다.
  * [첨부파일 업로드 API](http://localhost:8080/swagger-ui/index.html#/%EA%B3%B5%EC%A7%80%EC%82%AC%ED%95%AD%20(%EA%B4%80%EB%A6%AC%EC%9E%90%EC%9A%A9)/uploadNoticeAttachment)

## 핵심 문제 해결 전략
공지사항 조회와 관련된 대량의 트래픽이 유입될 것을 전제로 다음과 같은 문제들을 가정하였습니다.

### 관리용 API 와 서비스용 API 분리
* 관리용 API 는 일반적으로 실시간 (DB 직접 조회), 서비스용은 다양한 캐싱전략이 활용되므로 이를 별개의 API 로 분리했습니다.
  * [관리자용 API](http://localhost:8080/swagger-ui/index.html#/%EA%B3%B5%EC%A7%80%EC%82%AC%ED%95%AD%20(%EA%B4%80%EB%A6%AC%EC%9E%90%EC%9A%A9))
  * [서비스용 API](http://localhost:8080/swagger-ui/index.html#/%EA%B3%B5%EC%A7%80%EC%82%AC%ED%95%AD%20(%EC%84%9C%EB%B9%84%EC%8A%A4%EC%9A%A9))

### 공지사항 조회 (목록, 상세) 대량 트래픽 유입의 경우
* 공지사항 목록
  * 현재 노출중이거나, 앞으로 노출 예정인 모든 공지사항의 ID 목록을 notice:visible_ids 라는 key 로 저장합니다.
  * 해당 ID 값들을 이용하여 Redis + DB 에서 개별 공지사항 상세를 조회합니다. 
  * 관리 API 를 통해 공지사항이 새로 생성되거나 수정 / 삭제될 때, 공지사항의 노출 가능 여부에 따라 해당 목록을 갱신합니다.
  * 서비스용 API 에서 공지사항 목록을 요청하면 여기에 저장된 모든 ID 에 대해 공지사항을 조회한 후 Application 로직 내에서 검색 조건에 따라 필터처리합니다.
* 공지사항 상세
  * 공지사항 ID 별로 notice:{noticeId} 라는 Key 로 공지사항 정보 전체를 저장합니다.
* 관리용 API 와 서비스용 API 분리
  * 관리용 API 는 일반적으로 실시간 (DB 직접 조회), 서비스용은 다양한 캐싱전략이 활용되므로 이를 별개의 API 로 분리했습니다.
    * [관리자용 API](http://localhost:8080/swagger-ui/index.html#/%EA%B3%B5%EC%A7%80%EC%82%AC%ED%95%AD%20(%EA%B4%80%EB%A6%AC%EC%9E%90%EC%9A%A9))
    * [서비스용 API](http://localhost:8080/swagger-ui/index.html#/%EA%B3%B5%EC%A7%80%EC%82%AC%ED%95%AD%20(%EC%84%9C%EB%B9%84%EC%8A%A4%EC%9A%A9))

#### 위와 같은 설계 목적
* 공지사항이라는 특성 상 서비스 화면에서 엄청난 대량의 데이터를 보여줄 필요가 없습니다. 따라서 노출하고자 하는 목록을 별도로 캐싱하여 관리합니다.
* 공지사항의 노출 기간이 시간이 지남에 따라 유동적이므로, 캐시에는 가능한 모든 데이터를 저장해두고, API 요청시마다 시간에 따라 적절하게 필터하도록 하여 실시간성을 보장합니다.
* 모든 공지사항의 상세 정보는 notice:{noticeId} 에만 저장되고, 목록 조회시에도 이를 활용합니다. 이는 Redis 데이터가 목록용 / 상세용으로 이원화되어 관리가 복잡해지지 않도록 하기 위함입니다.

### 서비스용 API 에서 공지사항 목록 조회 시 서로 다른 페이지에서 중복노출이 되는 경우
* visible_ids 는 관리자 페이지에서 운영함에 따라 상시로 변경될 수 있으므로 페이지를 넘어가는 과정에서 의도치 않게 중복된 항목이 노출될 수 있습니다.
* 이를 방지하기 위해 next_cursor 를 이용한 커서 방식 페이징 조회로 구현이 되었습니다.
  * next_cursor 는 앞 페이지의 가장 마지막 공지사항에 대한 id 이며, 다음 페이지의 항목들은 모두 이 id 보다 작은 값들을 가지게 되므로 앞 페이지에 노출된 항목이 뒷 페이지에 나오지 않는 장치로 충분한 역할을 할 수 있습니다.


### 공지사항 조회 대량 트래픽에 대한 조회 수 집계
* 사용자가 공지사항 상세를 조회할 때마다 조회 수를 1씩 증가시켜야 하는데, 이 트래픽이 높을 수 있기 때문에 다음과 같이 중간 장치를 이용합니다.
  * NoticeStatisticsManager 를 통해 조회 수 증가 대상 공지사항 ID 별로 AtomicLong 을 이용해 원자적으로 데이터를 보존
  * 보존된 AtomicLong 값들은 스케줄러 (매 3초 간격) 를 통해 모아서 DB 와 Redis 에 업데이트 치는 절차로 실행
  * Redis 에 업데이트 치는 과정의 동시성 문제 해소를 위해 Redisson 의 분산락 을 활용
  * 이를 통해 사용자는 준 실시간으로 갱신되는 조회 수 를 제공받을 수 있습니다.

### 첨부 파일 저장소
* 일반적으로 S3 + CloudFront 등 CDN 을 이용하여 파일을 업로드 후 파일 다운로드 경로를 제공하지만 그 환경을 사용하기 어려우므로 비슷한 형태로 구현하였습니다.
  * 이 프로젝트에서는 attached_files 라는 테이블에 BLOB 형태로 저장하는 것으로 대체합니다.
  * 이 파일을 다운로드 받을 수 있도록 별도의 [다운로드 API](http://localhost:8080/swagger-ui/index.html#/%EA%B3%B5%EC%A7%80%EC%82%AC%ED%95%AD%20(%EC%84%9C%EB%B9%84%EC%8A%A4%EC%9A%A9)/downloadFile) 를 제공합니다.
  * 추후 CDN 을 활용하는 부분으로 대체되면 해당 테이블은 fade-out 이 가능해집니다.

### 제목, 내용 검색
* 제목 + 내용 검색은 관리자 페이지에서만 사용한다고 가정하고, 단순 LIKE 검색으로 구현하였습니다.
  * 사유는, 별도의 검색엔진까지 도입하기에는 예측되는 데이터 적재량이 적은 편에 속하며,
  * MySQL 의 ngram 등을 활용하면 비즈니스 로직 등이 RDBMS 에 종속적이 되므로 단순하게 LIKE 검색으로 구현하였습니다. 

### 공지사항 별 첨부파일을 n개 등록할 때
* 첨부파일은 화면에서 사용자가 파일 1건을 업로드할 때마다 서버로 요청되어 실제 업로드까지 실행됩니다.
  * 이후 실제로 공지사항 첨부파일에 대해 저장되는 정보는 위 요청에서 제공된 **파일 다운로드 경로** 입니다.
  * 이는 여러 파일을 한번에 저장할 때 spring.servlet.max-file-size 를 작게 가져갈 수 있고, 사용자가 체감하는 처리 속도도 더 빠르게 됩니다. 


## DB 테이블 목록
### notice
* 공지사항 정보 테이블
```sql
CREATE TABLE `notice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text NOT NULL COMMENT '내용',
  `created_at` datetime(6) NOT NULL COMMENT '등록 일시',
  `created_by` varchar(255) NOT NULL COMMENT '등록자',
  `ends_at` datetime(6) NOT NULL COMMENT '공지사항 종료 일시',
  `starts_at` datetime(6) NOT NULL COMMENT '공지사항 시작 일시',
  `title` varchar(255) NOT NULL COMMENT '제목',
  `updated_at` datetime(6) DEFAULT NULL COMMENT '최종 수정 일시',
  `updated_by` varchar(255) DEFAULT NULL COMMENT '최종 수정자',
  `view_count` bigint NOT NULL COMMENT '조회 수',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

* 공지사항 별 첨부파일 정보 테이블
```sql
CREATE TABLE `notice_attachment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `file_path` varchar(255) DEFAULT NULL COMMENT '파일 경로',
  `notice_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_notice_attachment_notice` (`notice_id`),
  CONSTRAINT `fk_notice_attachment_notice` FOREIGN KEY (`notice_id`) REFERENCES `notice` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

* 첨부파일 저장용 테이블
```sql
CREATE TABLE `attached_files` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `file_data` mediumblob COMMENT '파일 데이터',
  `name` varchar(255) DEFAULT NULL COMMENT '파일 이름',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
```

## 프로젝트 구조
```markdown
src/main/kotlin
├── com.rsupport.pretest.kyusubkim
│   ├── common : 공통 클래스
│   │   ├── config : 각종 Configuration 클래스
│   │   └── exception : 예외 처리
│   ├── notice : 공지사항 관련 기능
│   │   ├── application : UseCase 등 비즈니스 로직을 실행하는 서비스 계층
│   │   ├── domain : 도메인 객체의 동작 및 상태를 정의하는 계층
│   │   ├── infrastructure : 데이터베이스, API 클라이언트 등 외부 시스템과의 연결 계층
│   │   └── presentation : RestController 등 외부 요청과 응답을 처리하는 계층
└── KyusubkimApplication.kt : 메인 SpringBootApplication 

src/main/resources
├── application.yml : 어플리케이션 공통 설정
└── application-test.yml : Unit Test 용 설정
```