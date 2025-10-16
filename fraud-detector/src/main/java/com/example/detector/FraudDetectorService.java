package com.example.detector;

import com.example.common.kafka.GsonDeserializer;
import com.example.common.kafka.Transaction;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class FraudDetectorService {

    public static final String KAFKA_TOPIC = "transactions";
    // Limite de 2 transações...
    private static final int TRANSACTION_LIMIT = 2;
    // ...em uma janela de 10 segundos.
    private static final long TIME_WINDOW_MS = 10000;

    // Mapa para armazenar o estado: userId -> [contagem, timestamp da primeira transação na janela]
    private static final Map<String, UserTransactionState> userState =
        new ConcurrentHashMap<>();

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            StringDeserializer.class.getName()
        );
        props.put(
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
            GsonDeserializer.class.getName()
        );
        props.put(GsonDeserializer.TYPE_CONFIG, Transaction.class.getName());
        props.put(
            ConsumerConfig.GROUP_ID_CONFIG,
            "fraud-detector-group-" + UUID.randomUUID()
        );
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (
            KafkaConsumer<String, Transaction> consumer = new KafkaConsumer<>(
                props
            )
        ) {
            consumer.subscribe(Collections.singletonList(KAFKA_TOPIC));
            System.out.println(
                "Detector de fraudes com lógica de estado iniciado..."
            );

            while (true) {
                ConsumerRecords<String, Transaction> records = consumer.poll(
                    Duration.ofMillis(100)
                );
                for (ConsumerRecord<String, Transaction> record : records) {
                    Transaction transaction = record.value();
                    String userId = transaction.getUserId();
                    long currentTime = System.currentTimeMillis();

                    // Pega o estado atual do usuário ou cria um novo se não existir
                    UserTransactionState state = userState.getOrDefault(
                        userId,
                        new UserTransactionState()
                    );

                    // Verifica se a janela de tempo do estado atual expirou
                    if (
                        currentTime - state.getWindowStartTimestamp() >
                        TIME_WINDOW_MS
                    ) {
                        // Janela expirou, reseta o estado para a transação atual
                        state.reset(currentTime);
                    }

                    // Incrementa a contagem de transações na janela atual
                    state.incrementCount();

                    // Aplica a regra de fraude
                    if (state.getCount() > TRANSACTION_LIMIT) {
                        System.out.println(
                            "----------------------------------------"
                        );
                        System.err.println("!!!! FRAUDE DETECTADA !!!!");
                        System.err.println("Usuário: " + userId);
                        System.err.println("Transação: " + transaction);
                        System.out.println(
                            "----------------------------------------"
                        );
                    } else {
                        System.out.println(
                            "----------------------------------------"
                        );
                        System.out.println(
                            "Transação normal processada para o usuário: " +
                                userId
                        );
                        System.out.println(
                            "Contagem na janela: " + state.getCount()
                        );
                    }

                    // Atualiza o estado do usuário no mapa
                    userState.put(userId, state);
                }
            }
        }
    }

    // Classe interna para manter o estado de cada usuário
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
