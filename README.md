# fory-test

Apache Fory(Java)와 Jackson(JSON)의 직렬화/역직렬화 성능을 비교하는 샘플 프로젝트입니다.

## 프로젝트 개요

- **스텝1**: `SerializationBenchmark`에서 로컬 환경(파일만) 직렬화·역직렬화 시간을 측정.
- **스텝2**: Spring Boot 서버/클라이언트를 통해 HTTP 업로드(이미지) 경로에서 JSON vs Fory를 비교.
- 사용 기술: Java 21, Spring Boot 3.5.7, Fory 0.13.2, Jackson.

## 디렉터리 구조

- `src/main/java/com/example/forytest/common` 공통 DTO/직렬화 유틸.
- `src/main/java/com/example/forytest/step1` 직렬화 마이크로 벤치마크.
- `src/main/java/com/example/forytest/step2` 서버/클라이언트 엔드투엔드 벤치마크.
- `src/main/resources/images` 테스트 이미지 (gitignore 제외).

## 실행 방법 (스텝1 직렬화 벤치마크)

1. 테스트할 이미지를 `src/main/resources/images/big_sample.jpg`에 준비합니다.
2. Gradle Wrapper 실행:
   ```
   ./gradlew --no-daemon runSerializationBenchmark -Dorg.gradle.jvmargs=-Xmx1g
   ```
   (메모리 여유가 없으면 `-Dorg.gradle.jvmargs=-Xmx2g`로 조정)

### 최근 벤치마크 결과 (100회 반복)

- JSON: 직렬화 14.66 ms, 역직렬화 13.68 ms
- Fory: 직렬화 7.37 ms, 역직렬화 1.70 ms
- 초기 1회만 Fory 코드 로딩/컴파일로 인한 오버헤드 발생(총 28 ms + 109 ms)

## 스텝2: HTTP 엔드투엔드 벤치마크

- 서버 기동: `./gradlew bootRun`
- 클라이언트 벤치마크: `./gradlew runE2EBenchmark` (또는 `BenchmarkClient` main 실행)
- 동작: `big_size_image.jpg` 1개를 JSON/Fory로 각각 업로드하여 처리.
- 측정 방식: 워밍업 3회 후 본 측정 50회. 각 요청 시간(ms)을 로그하고, 본 측정 40회의 평균/표준편차/최대값을 JSON/Fory별로 출력.
- 관찰 예시(단일 스레드, 로컬): JSON ≈ 30 ms, Fory ≈ 9 ms.
- RestTemplate 커넥션 풀 적용은 스텝3에서 진행 예정.

## 주의/팁

- 반복 횟수가 커지면 JVM JIT/클래스 로딩이 안정화되어 시간이 평탄해집니다. 비교는 워밍업 이후 구간으로 보는 것이 좋습니다.
- 동시성(멀티스레드) 시나리오는 별도 테스트를 권장합니다. 연결 풀을 쓰면 반복/동시 요청에서 오버헤드가 더 줄어듭니다(스텝3 예정).
