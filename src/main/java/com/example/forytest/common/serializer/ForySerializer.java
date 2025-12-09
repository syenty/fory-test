package com.example.forytest.common.serializer;

import com.example.forytest.common.model.ImagePayload;
import org.apache.fory.BaseFory;
import org.apache.fory.Fory;
import org.apache.fory.config.Language;

/**
 * Apache Fory ??? ??. ????? ????? ?? ????.
 */
public class ForySerializer {
    private final BaseFory fory;

    public ForySerializer() {
        this.fory = Fory.builder()
                .withLanguage(Language.JAVA)
                .requireClassRegistration(true)
                // ??? ???? ???? buildThreadSafeFory ??
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
