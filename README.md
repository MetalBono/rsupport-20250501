RSUPPORT 과제
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
작성 예정

## 핵심 문제 해결 전략
작성 예정

## 테이블 스키마
작성 예정

## 프로젝트 구조
```markdown
src/main/kotlin
├── com.rsupport.pretest.kyusubkim
│   ├── common : 공통 클래스
│   ├── notice : 공지사항 관련 기능
│   │   ├── application : UseCase 등 비즈니스 로직을 실행하는 서비스 계층
│   │   ├── domain : 도메인 객체의 동작 및 상태를 정의하는 계층
│   │   ├── infrastructure : 데이터베이스, API 클라이언트 등 외부 시스템과의 연결 계층
│   │   └── presentation : RestController 등 외부 요청과 응답을 처리하는 계층
└── KyusubkimApplication.kt : 메인 SpringBootApplication 

src/main/resources
└── application.yml : 어플리케이션 공통 설정
```