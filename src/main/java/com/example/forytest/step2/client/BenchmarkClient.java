package com.example.forytest.step2.client;

import com.example.forytest.common.model.ImagePayload;
import com.example.forytest.common.serializer.ForySerializer;
import com.example.forytest.common.serializer.JsonSerializer;
import com.example.forytest.common.util.ImageLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class BenchmarkClient {
    private static final String BASE = "http://localhost:8080/api";
    private static final int WARMUP_COUNT = 3;
    private static final int RUN_COUNT = 50;

    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();
        String imagePath = Paths.get("src", "main", "resources", "images", "big_size_image.jpg").toString();
        byte[] data = ImageLoader.load(imagePath);
        ImagePayload payload = new ImagePayload("big_size_image.jpg", data);

        JsonSerializer jsonSerializer = new JsonSerializer();
        ForySerializer forySerializer = new ForySerializer();
        List<Double> jsonTimes = new ArrayList<>();
        List<Double> foryTimes = new ArrayList<>();

        System.out.printf("Warmup start (%d rounds)%n", WARMUP_COUNT);
        for (int i = 1; i <= WARMUP_COUNT; i++) {
            logCall(restTemplate, i, "/json", "json",
                    jsonSerializer.serialize(payload), MediaType.APPLICATION_JSON_VALUE, true);
            logCall(restTemplate, i, "/fory", "fory",
                    forySerializer.serialize(payload), MediaType.APPLICATION_OCTET_STREAM_VALUE, true);
        }

        System.out.printf("Benchmark start (%d rounds)%n", RUN_COUNT);
        for (int i = 1; i <= RUN_COUNT; i++) {
            double jsonMs = logCall(restTemplate, i, "/json", "json",
                    jsonSerializer.serialize(payload), MediaType.APPLICATION_JSON_VALUE, false);
            jsonTimes.add(jsonMs);

            double foryMs = logCall(restTemplate, i, "/fory", "fory",
                    forySerializer.serialize(payload), MediaType.APPLICATION_OCTET_STREAM_VALUE, false);
            foryTimes.add(foryMs);
        }

        printStats("json", jsonTimes);
        printStats("fory", foryTimes);
    }

    private static double logCall(
            RestTemplate restTemplate,
            int iteration,
            String path,
            String label,
            byte[] body,
            String contentType,
            boolean warmup) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, contentType);

        long start = System.nanoTime();
        String response = restTemplate.postForObject(BASE + path, new HttpEntity<>(body, headers), String.class);
        long elapsed = System.nanoTime() - start;
        double elapsedMs = elapsed / 1_000_000.0;

        String phase = warmup ? "warmup" : "run";
        System.out.printf("[%s %d][%s] response=%s time=%.3f ms%n",
                phase, iteration, label, response, elapsedMs);
        return elapsedMs;
    }

    private static void printStats(String label, List<Double> times) {
        if (times.isEmpty()) {
            System.out.printf("[summary][%s] no data%n", label);
            return;
        }

        double sum = 0.0;
        double max = Double.MIN_VALUE;
        for (double t : times) {
            sum += t;
            if (t > max) {
                max = t;
            }
        }
        double mean = sum / times.size();
        double varianceSum = 0.0;
        for (double t : times) {
            double diff = t - mean;
            varianceSum += diff * diff;
        }
        double stddev = Math.sqrt(varianceSum / times.size());

        System.out.printf("[summary][%s] count=%d mean=%.3f ms stddev=%.3f ms max=%.3f ms%n",
                label, times.size(), mean, stddev, max);
    }
}
