# fory-test

Apache Fory(Java)와 Jackson(JSON)의 직렬화/역직렬화 성능을 비교하는 샘플 프로젝트입니다.

## 프로젝트 개요

- **스텝1**: `SerializationBenchmark`로 로컬 환경(파일 기반) 직렬화·역직렬화 시간 측정.
- **스텝2**: Spring Boot 서버/클라이언트를 통한 HTTP 업로드 경로에서 JSON vs Fory 비교.
- **스텝3**: 스텝2 흐름은 그대로 두고, 클라이언트 `RestTemplate`에 Apache HttpClient5 커넥션 풀을 적용해 연결 재사용 성능을 확인.

## 디렉터리 구조

- `src/main/java/com/example/forytest/common` 공통 DTO/직렬화 유틸.
- `src/main/java/com/example/forytest/step1` 직렬화 마이크로 벤치마크.
- `src/main/java/com/example/forytest/step2` 서버/클라이언트 엔드투엔드 벤치마크.
- `src/main/java/com/example/forytest/step3` 커넥션 풀 적용 엔드투엔드 벤치마크(서버는 스텝2 것 재사용, 클라이언트만 다름).
- `src/main/resources/images` 테스트 이미지 (gitignore 제외).

## 실행 방법

### 스텝1 (직렬화 마이크로 벤치마크)

1. 테스트 이미지를 `src/main/resources/images/big_sample.jpg`에 준비합니다.
2. Gradle Wrapper 실행:
   ```
   ./gradlew --no-daemon runSerializationBenchmark -Dorg.gradle.jvmargs=-Xmx1g
   ```
   (메모리가 부족하면 `-Dorg.gradle.jvmargs=-Xmx2g` 등으로 조정)

### 스텝2 (HTTP 엔드투엔드)

- 서버 기동: `./gradlew bootRun` (메인 클래스: `step2.server.ServerApplication`)
- 클라이언트 벤치마크: `./gradlew runE2EBenchmark` (또는 `step2.client.BenchmarkClient` main)
- 동작: `big_size_image.jpg` 1개를 JSON/Fory로 각각 업로드.
- 측정: 워밍업 3회 후 본 측정 50회. 각 요청 시간(ms) 로그 + JSON/Fory별 mean/stddev/max 요약.
- 관찰 예시(단일 스레드, 로컬): JSON 약 30 ms, Fory 약 9 ms.

### 스텝3 (HTTP 엔드투엔드 + 커넥션 풀)

- 서버: 스텝2 서버를 그대로 사용합니다(`./gradlew bootRun`).
- 클라이언트 벤치마크: `./gradlew runE2EBenchmarkWithConnectionPool` (또는 `step3.client.BenchmarkClient` main)
- 측정 방식은 스텝2와 동일(워밍업 3회 + 본 측정 50회, mean/stddev/max 출력).
- `RestTemplate`에 Apache HttpClient5 커넥션 풀 적용: maxTotal 50, route당 20. 반복·동시 요청에서 연결 생성/해제 오버헤드가 줄어드는 효과를 확인할 수 있습니다.

## 최근 벤치마크 예시 (스텝1, 100회 반복)

- JSON: 직렬화 14.66 ms, 역직렬화 13.68 ms
- Fory: 직렬화 7.37 ms, 역직렬화 1.70 ms
- 초기 1회만 Fory 코드 로딩/컴파일로 인한 오버헤드 발생(총 28 ms + 109 ms)

## 주의/팁

- 반복 횟수가 충분해야 JVM JIT/클래스 로딩이 안정화됩니다. 비교는 워밍업 이후 구간 기준으로 보는 것이 좋습니다.
- 동시성(멀티스레드) 시나리오는 별도 테스트를 권장합니다. 커넥션 풀을 적용하면 반복/동시 요청 시 연결 생성/해제 비용을 줄일 수 있습니다.
