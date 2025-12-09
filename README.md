# fory-test

https://github.com/apache/fory

1단계: 직렬화/역직렬화만 순수하게 비교 (로컬, JMH 추천)

동일한 Java 객체(예: ImagePayload { byte[] data; String name; ... })

JSON: Jackson / Gson

Fory: Fory Java 라이브러리

코드 구조 예시 느낌:

// 의사코드 느낌
ImagePayload payload = loadBigImage(); // byte[]로 읽어서 객체에 넣기

// JSON
long t1 = System.nanoTime();
byte[] jsonBytes = objectMapper.writeValueAsBytes(payload);
ImagePayload fromJson = objectMapper.readValue(jsonBytes, ImagePayload.class);
long t2 = System.nanoTime();

// Fory
long t3 = System.nanoTime();
byte[] foryBytes = forySerializer.serialize(payload);
ImagePayload fromFory = forySerializer.deserialize(foryBytes);
long t4 = System.nanoTime();

System.out.println("JSON serialize+deserialize: " + (t2 - t1) / 1_000_000.0 + " ms");
System.out.println("FORY serialize+deserialize: " + (t4 - t3) / 1_000_000.0 + " ms");
System.out.println("SIZE json=" + jsonBytes.length + " fory=" + foryBytes.length);

여기서는:

같은 JVM, 같은 머신, 같은 데이터

네트워크 없음
→ Fory의 “순수한 이득”이 얼마나 되는지 깔끔하게 볼 수 있음.

이건 JMH로 bench를 짜면 더 정확해지지만, 간단한 main 메서드 반복 루프만으로도 상대 차이는 충분히 느껴질 거야.

2단계: 네트워크 포함 E2E 테스트 (네 아이디어 활용)

여기서 네가 말한 서비스 2개 구조를 쓰면 좋아.

구조

Service A (Sender)

파일 시스템에서 큰 이미지 읽기

ImagePayload 객체 만들기

JSON / Fory 중 하나로 직렬화

HTTP POST로 Service B에 전송

A에서 start/end 모두 찍어서 “보내기 직전 ~ 응답 받는 순간” 측정

Service B (Receiver)

요청 바디 수신

해당 포맷(JSON/Fory)으로 역직렬화

단순히 "ok" 응답 반환 (처리시간 최소화)

타임 측정은 이렇게:
A 서버

1. start = System.nanoTime()
2. HTTP POST (body=serialized bytes)
3. 응답 수신
4. end = System.nanoTime()

elapsed = end - start // 네트워크 + 직렬화/역직렬화 + B 처리 포함 전체 시간

이렇게 하면:

클라이언트(A) 기준의 엔드 투 엔드 latency를 비교할 수 있음

B에서 시간 안 찍어도 됨 (시간 동기 걱정 X)

이때 주의할 점

한 번만 측정하면 안 됨

각 방식(JSON/Fory)당 수백~수천 번 반복해서 평균/표준편차를 보는 게 좋아.

예:

warming 100회 버리고

그 뒤 1000회 측정 → 평균(latency), p95, p99도 계산해보면 더 멋짐.

네트워크 환경 고정

둘 다 같은 서버(예: Docker compose 내) 에 두고
A→B를 localhost 혹은 동일 브리지 네트워크에서 호출하면 좋음.

외부 네트워크(인터넷, 와이파이)를 타면 랜덤 노이즈 너무 큼.

페이로드는 동일하게

같은 이미지 byte 배열 사용

JSON 쪽은 Base64 등 필요한 인코딩을 거치더라도 항상 동일한 데이터로.

4. JSON vs Fory 비교 시 “스토리” 만들기

벤치마크를 단순 수치뿐 아니라, 설명 가능한 스토리로 만들면 포트폴리오용으로도 좋아.

예를 들면:

직렬화/역직렬화 기준

JSON: 평균 15ms, 1MB 페이로드

Fory: 평균 2ms, 400KB 페이로드
→ CPU 사용량/GC도 비교 가능하면 더 좋고

네트워크 포함 E2E 기준

같은 네트워크, 같은 서버

JSON: 평균 40ms

Fory: 평균 28ms
→ 네트워크 비중이 커져서 차이가 줄지만, 그래도 유의미한 개선이 있음을 보여줌

그 뒤 이렇게 결론:

“순수 직렬화/역직렬화 시엔 Fory가 JSON보다 약 X~Y배 빠르고, 네트워크까지 포함한 전체 요청-응답 시나리오에서도 약 N% 레이턴시 개선이 있었다. 특히 대용량 바이너리(이미지) 기반 이벤트 처리에서 직렬화 포맷 선택이 의미 있는 영향을 준다는 걸 확인할 수 있었다.”
