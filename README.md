# fory-test

Apache Fory(Java)와 Jackson(JSON)의 직렬화/역직렬화 성능을 비교하는 샘플 프로젝트입니다.

## 프로젝트 개요

- **스텝1**: `SerializationBenchmark`로 로컬 환경(파일 기반) 직렬화·역직렬화 시간 측정.
- **스텝2**: Spring Boot 서버/클라이언트를 통한 HTTP 업로드 경로에서 JSON vs Fory 비교 (단일 스레드).
- **스텝3**: 스텝2와 동일 흐름에 클라이언트 `RestTemplate` 커넥션 풀(HttpClient5) 적용 (단일 스레드).
- **스텝4**: 스텝2의 멀티스레드 버전(총 소요 시간 포함).
- **스텝5**: 스텝3의 멀티스레드 버전(총 소요 시간 포함, 커넥션 풀).

## 디렉터리 구조

- `src/main/java/com/example/forytest/common` 공통 DTO/직렬화 유틸.
- `src/main/java/com/example/forytest/step1` 직렬화 마이크로 벤치마크.
- `src/main/java/com/example/forytest/step2` 서버/클라이언트 엔드투엔드 벤치마크(단일 스레드).
- `src/main/java/com/example/forytest/step3` 커넥션 풀 적용 엔드투엔드 벤치마크(단일 스레드, 서버는 스텝2 재사용).
- `src/main/java/com/example/forytest/step4` 엔드투엔드 멀티스레드 벤치마크.
- `src/main/java/com/example/forytest/step5` 엔드투엔드 멀티스레드 + 커넥션 풀 벤치마크.
- `src/main/resources/images` 테스트 이미지 (gitignore 제외).

## 실행 방법

### 스텝1 (직렬화 마이크로 벤치마크)

1. 테스트 이미지를 `src/main/resources/images/big_sample.jpg`에 준비합니다.
2. Gradle Wrapper 실행:
   ```
   ./gradlew --no-daemon runSerializationBenchmark -Dorg.gradle.jvmargs=-Xmx1g
   ```
   (메모리가 부족하면 `-Dorg.gradle.jvmargs=-Xmx2g` 등으로 조정)

### 스텝2 (HTTP 엔드투엔드, 단일 스레드)

- 서버: `./gradlew bootRun` (메인 클래스: `step2.server.ServerApplication`)
- 클라이언트: `./gradlew runE2EBenchmark` (또는 `step2.client.BenchmarkClient` main)
- 워밍업 3회 후 본 측정 500회. per-request 시간(ms) 로그 + mean/stddev/max + 전체 소요 시간 출력.

### 스텝3 (HTTP 엔드투엔드, 단일 스레드 + 커넥션 풀)

- 서버: 스텝2 서버 재사용 (`./gradlew bootRun`).
- 클라이언트: `./gradlew runE2EBenchmarkWithConnectionPool` (또는 `step3.client.BenchmarkClient` main)
- 워밍업 3회 후 본 측정 500회. per-request 시간(ms) 로그 + mean/stddev/max + 전체 소요 시간 출력.
- 클라이언트 `RestTemplate`에 HttpClient5 커넥션 풀 적용: maxTotal 50, route당 20.

### 스텝4 (HTTP 엔드투엔드, 멀티스레드)

- 서버: 스텝2 서버 재사용 (`./gradlew bootRun`).
- 클라이언트: `./gradlew runE2EBenchmarkStep4` (또는 `step4.client.BenchmarkClient` main)
- 워밍업 3회 후 본 측정 500회, 스레드 8개 기본. per-request 시간 로그 + mean/stddev/max + 전체 소요 시간 출력.

### 스텝5 (HTTP 엔드투엔드, 멀티스레드 + 커넥션 풀)

- 서버: 스텝2 서버 재사용 (`./gradlew bootRun`).
- 클라이언트: `./gradlew runE2EBenchmarkStep5` (또는 `step5.client.BenchmarkClient` main)
- 스텝4와 동일한 멀티스레드 설정/측정 방식 + 커넥션 풀 적용. per-request 로그 + mean/stddev/max + 전체 소요 시간 출력.

## 최근 벤치마크 예시 (스텝1, 100회 반복)

- JSON: 직렬화 14.66 ms, 역직렬화 13.68 ms
- Fory: 직렬화 7.37 ms, 역직렬화 1.70 ms
- 초기 1회만 Fory 코드 로딩/컴파일로 인한 오버헤드 발생(총 28 ms + 109 ms)

## 최근 벤치마크 예시 (스텝2~5, 500회 반복)

- **스텝2**(단일 스레드, 커넥션 풀 없음): JSON mean 25.44 ms (stddev 2.05, max 42.83), Fory mean 7.59 ms (stddev 1.21, max 14.76), total 22,117 ms
- **스텝3**(단일 스레드, 커넥션 풀 있음): JSON mean 24.16 ms (stddev 1.87, max 37.60), Fory mean 6.56 ms (stddev 1.17, max 14.83), total 20,655 ms
- **스텝4**(멀티스레드 8개, 커넥션 풀 없음): JSON mean ~62–64 ms, Fory mean ~15–16 ms, total ~6.7 s
- **스텝5**(멀티스레드 8개, 커넥션 풀 있음): JSON/Fory per-request는 스텝4와 유사하지만 total이 더 낮음(커넥션 재사용 효과 기대 구간)

### 커넥션 풀 적용 효과 (스텝2 → 스텝3)

- 단일 스레드에서 평균/최대/표준편차가 소폭 개선되고, 전체 수행 시간도 약 1.5초 단축(500회 기준).
- 연결 생성/해제 오버헤드가 줄어드는 구간에서 효과가 나타나며, 동시성/반복이 많을수록 체감이 커질 수 있음.

### 멀티스레드 적용 효과 (스텝4/5)

- 스레드당 지연은 커지지만, 동일 횟수를 더 짧은 총 시간에 처리(스텝4/5 total ~6.6–6.7s vs 단일 스레드 ~20–22s).
- 커넥션 풀이 있는 스텝5가 없는 스텝4보다 총 시간이 더 짧거나 안정적일 수 있음. 멀티스레드/동시성 시나리오에서 연결 재사용 이점이 두드러짐.

## 주의/팁

- 반복 횟수가 충분해야 JVM JIT/클래스 로딩이 안정화됩니다. 비교는 워밍업 이후 구간 기준으로 보는 것이 좋습니다.
- 동시성(멀티스레드) 시나리오는 별도 테스트를 권장합니다. 커넥션 풀을 적용하면 반복/동시 요청 시 연결 생성/해제 비용을 줄일 수 있습니다.
