package com.example.forytest.common.serializer;

import com.example.forytest.common.model.ImagePayload;
import org.apache.fory.BaseFory;
import org.apache.fory.Fory;
import org.apache.fory.config.Language;

/**
 * Apache Fory 직렬화 래퍼. 인스턴스는 재사용하는 것이 권장된다.
 */
public class ForySerializer {
    private final BaseFory fory;

    public ForySerializer() {
        this.fory = Fory.builder()
                .withLanguage(Language.JAVA)
                .requireClassRegistration(true)
                // 스레드 세이프가 필요하면 buildThreadSafeFory 사용
                .build();
        this.fory.register(ImagePayload.class);
    }

    public byte[] serialize(ImagePayload payload) {
        return fory.serialize(payload);
    }

    public ImagePayload deserialize(byte[] bytes) {
        return (ImagePayload) fory.deserialize(bytes);
    }
}
