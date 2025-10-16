package com.example.detector;

import com.example.common.kafka.GsonDeserializer;
import com.example.common.kafka.GsonSerializer;
import com.example.common.kafka.Transaction;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class FraudDetectorService {

    public static final String INPUT_TOPIC = "transactions";
    public static final String OUTPUT_TOPIC = "fraud_alerts"; // Novo tópico de saída
    private static final int TRANSACTION_LIMIT = 2;
    private static final long TIME_WINDOW_MS = 10000;

    private static final Map<String, UserTransactionState> userState =
        new ConcurrentHashMap<>();

    public static void main(String[] args) {
        // Configurações do Consumidor
        Properties consumerProps = new Properties();
        consumerProps.put(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
            "localhost:9092"
        );
        consumerProps.put(
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            StringDeserializer.class.getName()
        );
        consumerProps.put(
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            GsonDeserializer.class.getName()
        );
        consumerProps.put(
            GsonDeserializer.TYPE_CONFIG,
            Transaction.class.getName()
        );
        consumerProps.put(
            ConsumerConfig.GROUP_ID_CONFIG,
            "fraud-detector-group-" + UUID.randomUUID()
        );
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Configurações do Produtor (para enviar alertas)
        Properties producerProps = new Properties();
        producerProps.put(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            "localhost:9092"
        );
        producerProps.put(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class.getName()
        );
        producerProps.put(
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            GsonSerializer.class.getName()
        );

        // Cria o consumidor e o produtor
        try (
            KafkaConsumer<String, Transaction> consumer = new KafkaConsumer<>(
                consumerProps
            );
            KafkaProducer<String, Transaction> producer = new KafkaProducer<>(
                producerProps
            )
        ) {
            consumer.subscribe(Collections.singletonList(INPUT_TOPIC));
            System.out.println(
                "Detector de fraudes com publicação de alertas iniciado..."
            );

            while (true) {
                ConsumerRecords<String, Transaction> records = consumer.poll(
                    Duration.ofMillis(100)
                );
                for (ConsumerRecord<String, Transaction> record : records) {
                    Transaction transaction = record.value();
                    String userId = transaction.getUserId();
                    long currentTime = System.currentTimeMillis();

                    UserTransactionState state = userState.getOrDefault(
                        userId,
                        new UserTransactionState()
                    );

                    if (
                        currentTime - state.getWindowStartTimestamp() >
                        TIME_WINDOW_MS
                    ) {
                        state.reset(currentTime);
                    }
                    state.incrementCount();

                    if (state.getCount() > TRANSACTION_LIMIT) {
                        System.out.println(
                            "----------------------------------------"
                        );
                        System.err.println(
                            "!!!! FRAUDE DETECTADA E PUBLICADA !!!!"
                        );
                        System.err.println(
                            "Usuário: " +
                                userId +
                                " | Transação: " +
                                transaction.getTransactionId()
                        );
                        System.out.println(
                            "----------------------------------------"
                        );

                        // Publica a transação fraudulenta no tópico de alertas
                        producer.send(
                            new ProducerRecord<>(
                                OUTPUT_TOPIC,
                                userId,
                                transaction
                            )
                        );
                    } else {
                        System.out.println(
                            "Transação normal processada para o usuário: " +
                                userId
                        );
                    }

                    userState.put(userId, state);
                }
            }
        }
    }

    private static class UserTransactionState {

        private int count;
        private long windowStartTimestamp;

        public UserTransactionState() {
            this.count = 0;
            this.windowStartTimestamp = 0;
        }

        public void incrementCount() {
            this.count++;
        }

        public void reset(long newTimestamp) {
            this.count = 1;
            this.windowStartTimestamp = newTimestamp;
        }

        public int getCount() {
            return count;
        }

        public long getWindowStartTimestamp() {
            return windowStartTimestamp;
        }
    }
}
