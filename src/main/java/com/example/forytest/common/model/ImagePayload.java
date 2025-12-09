package com.example.forytest.common.model;

public class ImagePayload {
    private final String name;
    private final byte[] data;

    public ImagePayload(String name, byte[] data) {
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }
}
