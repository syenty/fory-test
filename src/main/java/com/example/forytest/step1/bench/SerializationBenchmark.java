package com.example.forytest.step1.bench;

import com.example.forytest.common.model.ImagePayload;
import com.example.forytest.common.serializer.ForySerializer;
import com.example.forytest.common.serializer.JsonSerializer;
import com.example.forytest.common.util.ImageLoader;
import java.nio.file.Paths;

public class SerializationBenchmark {
    private static final int ITERATIONS = 10;

    public static void main(String[] args) {
        String imagePath = Paths.get("src", "main", "resources", "images", "big_size_image.jpg").toString();
        byte[] imageBytes = ImageLoader.load(imagePath);
        ImagePayload payload = new ImagePayload("big_size_image.jpg", imageBytes);

        JsonSerializer jsonSerializer = new JsonSerializer();
        ForySerializer forySerializer = new ForySerializer();

        benchmark("JSON", () -> jsonSerializer.serialize(payload), bytes -> jsonSerializer.deserialize(bytes, ImagePayload.class));
        benchmark("Fory", () -> forySerializer.serialize(payload), forySerializer::deserialize);
    }

    private static <T> void benchmark(String label, SupplierWithException<byte[]> encoder, Deserializer<byte[], T> decoder) {
        long encodeStart = System.nanoTime();
        byte[][] encoded = new byte[ITERATIONS][];
        for (int i = 0; i < ITERATIONS; i++) {
            encoded[i] = encoder.get();
        }
        long encodeEnd = System.nanoTime();

        long decodeStart = System.nanoTime();
        for (byte[] bytes : encoded) {
            decoder.deserialize(bytes);
        }
        long decodeEnd = System.nanoTime();

        System.out.printf("[%s] encode avg: %.3f ms, decode avg: %.3f ms%n",
                label,
                (encodeEnd - encodeStart) / 1_000_000.0 / ITERATIONS,
                (decodeEnd - decodeStart) / 1_000_000.0 / ITERATIONS);
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get();
    }

    @FunctionalInterface
    private interface Deserializer<I, O> {
        O deserialize(I input);
    }
}
