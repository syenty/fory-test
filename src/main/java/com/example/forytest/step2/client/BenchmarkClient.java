package com.example.forytest.step2.client;

import com.example.forytest.common.model.ImagePayload;
import com.example.forytest.common.serializer.ForySerializer;
import com.example.forytest.common.serializer.JsonSerializer;
import com.example.forytest.common.util.ImageLoader;
import java.nio.file.Paths;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class BenchmarkClient {
    private static final String BASE = "http://localhost:8080/api";

    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();
        String imagePath = Paths.get("src", "main", "resources", "images", "big_size_image.jpg").toString();
        byte[] data = ImageLoader.load(imagePath);
        ImagePayload payload = new ImagePayload("big_size_image.jpg", data);

        JsonSerializer jsonSerializer = new JsonSerializer();
        ForySerializer forySerializer = new ForySerializer();

        call(restTemplate, "/json", jsonSerializer.serialize(payload), MediaType.APPLICATION_JSON_VALUE);
        call(restTemplate, "/fory", forySerializer.serialize(payload), MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }

    private static void call(RestTemplate restTemplate, String path, byte[] body, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, contentType);

        long start = System.nanoTime();
        String response = restTemplate.postForObject(BASE + path, new HttpEntity<>(body, headers), String.class);
        long elapsed = System.nanoTime() - start;

        System.out.printf("[%s] response=%s time=%.3f ms%n", path, response, elapsed / 1_000_000.0);
    }
}
