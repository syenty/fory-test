package com.example.forytest.common.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ImageLoader {
    private ImageLoader() {}

    public static byte[] load(String path) {
        try {
            return Files.readAllBytes(Path.of(path));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load image: " + path, e);
        }
    }
}
