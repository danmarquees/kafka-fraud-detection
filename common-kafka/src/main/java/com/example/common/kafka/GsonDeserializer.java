package com.example.common.kafka;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;

public class GsonDeserializer<T> implements Deserializer<T> {

    public static final String TYPE_CONFIG = "com.example.type_config";
    private final Gson gson = new GsonBuilder().create();
    private Class<T> type;

    // Novo construtor para o Kafka Streams
    public GsonDeserializer(Class<T> type) {
        this.type = type;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        if (type == null) {
            String typeName = String.valueOf(configs.get(TYPE_CONFIG));
            try {
                this.type = (Class<T>) Class.forName(typeName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(
                    "Type for deserialization not found: " + typeName,
                    e
                );
            }
        }
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        return gson.fromJson(new String(data), type);
    }
}
