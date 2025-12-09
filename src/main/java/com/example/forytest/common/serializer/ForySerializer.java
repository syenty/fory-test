package com.example.forytest.common.serializer;

import com.example.forytest.common.model.ImagePayload;
import java.util.Base64;

/**
 * Fory 래퍼 자리에 임시 구현을 둡니다. 실제 Fory 라이브러리를 연결하면 이 부분을 교체하세요.
 */
public class ForySerializer {
    public byte[] serialize(ImagePayload payload) {
        // 임시: 이름과 데이터를 단순 Base64로 묶어 직렬화
        String encoded = payload.getName() + ":" + Base64.getEncoder().encodeToString(payload.getData());
        return encoded.getBytes();
    }

    public ImagePayload deserialize(byte[] bytes) {
        String raw = new String(bytes);
        int separator = raw.indexOf(":");
        if (separator < 0) {
            throw new IllegalStateException("Invalid Fory payload");
        }
        String name = raw.substring(0, separator);
        byte[] data = Base64.getDecoder().decode(raw.substring(separator + 1));
        return new ImagePayload(name, data);
    }
}
