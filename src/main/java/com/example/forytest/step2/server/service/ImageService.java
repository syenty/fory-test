package com.example.forytest.step2.server.service;

import com.example.forytest.common.model.ImagePayload;
import com.example.forytest.common.serializer.ForySerializer;
import com.example.forytest.common.serializer.JsonSerializer;
import org.springframework.stereotype.Service;

@Service
public class ImageService {
    private final JsonSerializer jsonSerializer = new JsonSerializer();
    private final ForySerializer forySerializer = new ForySerializer();

    public String handleJson(byte[] bytes) {
        ImagePayload payload = jsonSerializer.deserialize(bytes, ImagePayload.class);
        return respond(payload);
    }

    public String handleFory(byte[] bytes) {
        ImagePayload payload = forySerializer.deserialize(bytes);
        return respond(payload);
    }

    private String respond(ImagePayload payload) {
        int size = payload.getData() == null ? 0 : payload.getData().length;
        return "processed:" + payload.getName() + ":" + size;
    }
}
