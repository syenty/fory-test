# fory-test

Apache Fory(Java)와 Jackson(JSON) 직렬화 성능을 비교하는 샘플입니다.

## 프로젝트 개요
- **스텝1**: `SerializationBenchmark`에서 이미지 페이로드 직렬화/역직렬화 평균 시간 측정.
- **스텝2**: Spring Boot 서버/클라이언트로 JSON vs Fory 네트워크 왕복 성능을 비교할 수 있는 구조(코드 뼈대만 포함).
- **사용 기술**: Java 21, Spring Boot 3.5.7, Fory 0.13.2, Jackson.

## 폴더 구조
- `src/main/java/com/example/forytest/common` … 공통 DTO/직렬화/유틸
- `src/main/java/com/example/forytest/step1` … 직렬화 벤치마크
- `src/main/java/com/example/forytest/step2` … 서버/클라이언트 예제 뼈대
- `src/main/resources/images` … 테스트 이미지 위치(.gitignore로 제외)

## 실행 방법 (스텝1 벤치마크)
1) 테스트할 이미지 파일을 `src/main/resources/images/big_sample.jpg`에 준비합니다.
2) Gradle Wrapper로 실행:
   ```
   ./gradlew --no-daemon runSerializationBenchmark -Dorg.gradle.jvmargs=-Xmx1g
   ```
   필요하면 힙을 더 키워서 실행합니다(예: `-Dorg.gradle.jvmargs=-Xmx2g`).

## 최근 벤치마크 결과 (100회 반복)
- JSON: 직렬화 14.66 ms, 역직렬화 13.68 ms
- Fory: 직렬화 7.37 ms, 역직렬화 1.70 ms
- 초기 1회만 Fory 코드 생성/컴파일 오버헤드 발생(약 28 ms + 109 ms).

해석: 반복을 늘린 상태에서 Fory가 직렬화·역직렬화 모두 JSON 대비 유의미하게 빠르게 측정되었습니다. 첫 실행의 코드 생성/컴파일 로그는 초기 오버헤드로 이후에는 재사용됩니다.

## 실제 서비스에서의 의미
- 일반적인 서비스는 “데이터는 매번 달라도 직렬화할 클래스(스키마)는 고정”입니다. 이 경우 코드 생성/컴파일은 클래스당 한 번만 일어나므로, 워밍업 이후 Fory의 빠른 역직렬화 이점이 그대로 유효합니다.
- 런타임에 새로운 타입이 계속 생기는 동적 워크로드라면 초기 오버헤드가 반복되어 JSON이 더 안정적일 수 있습니다.
- 실무 팁: 서버 기동 시 필요한 클래스들을 미리 등록하고 워밍업 요청을 흘려 Fory 코덱을 생성/컴파일해 두면 런타임에 오버헤드 없이 빠른 처리 성능을 얻을 수 있습니다.

## 추가 팁
- 워밍업 후 측정(예: 50회 워밍업 + N회 측정)을 적용하면 더 안정적인 수치를 얻을 수 있습니다.
- 이미지 크기를 바꿔가며 추세를 보는 것도 유용합니다.
