package com.example.forytest.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ImagePayload {
    private final String name;
    private final byte[] data;

    @JsonCreator
    public ImagePayload(
            @JsonProperty("name") String name,
            @JsonProperty("data") byte[] data) {
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
