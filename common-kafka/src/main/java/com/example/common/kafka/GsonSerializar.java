package com.example.common.kafka;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.kafka.common.serialization.Serializer;

public class GsonSerializer<T> implements Serializer<T> {

    private final Gson gson = new GsonBuilder().create();

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) {
            return null;
        }
        return gson.toJson(data).getBytes();
    }
}
