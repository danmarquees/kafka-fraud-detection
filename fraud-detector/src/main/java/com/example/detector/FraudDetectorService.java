package com.example.detector;

import com.example.common.kafka.GsonDeserializer;
import com.example.common.kafka.Transaction;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class FraudDetectorService {

    public static final String KAFKA_TOPIC = "transactions";

    public static void main(String[] args) {
        Properties props = new Properties();
        // Endereço do nosso broker Kafka
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // Desserializador da chave (String)
        props.put(
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
            StringDeserializer.class.getName()
        );
        // Nosso desserializador customizado para o valor (Transaction)
        props.put(
            Consumer.VALUE_DESERIALIZER_CLASS_CONFIG,
            GsonDeserializer.class.getName()
        );
        // Informa ao nosso desserializador qual objeto ele deve criar a partir do JSON
        props.put(GsonDeserializer.TYPE_CONFIG, Transaction.class.getName());
        // ID do grupo de consumidores. Permite que o Kafka controle o offset (onde paramos de ler)
        props.put(
            ConsumerConfig.GROUP_ID_CONFIG,
            "fraud-detector-group-" + UUID.randomUUID()
        );
        // Garante que vamos ler desde o início do tópico se for a primeira vez que este grupo consome
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (
            KafkaConsumer<String, Transaction> consumer = new KafkaConsumer<>(
                props
            )
        ) {
            // Inscreve o consumidor no tópico de transações
            consumer.subscribe(Collections.singletonList(KAFKA_TOPIC));

            System.out.println(
                "Detector de fraudes iniciado. Aguardando transações..."
            );

            // Loop infinito para consumir mensagens continuamente
            while (true) {
                // O método poll busca por novos registros no tópico. O timeout evita que o loop trave.
                ConsumerRecords<String, Transaction> records = consumer.poll(
                    Duration.ofMillis(100)
                );
                for (ConsumerRecord<String, Transaction> record : records) {
                    System.out.println(
                        "----------------------------------------"
                    );
                    System.out.println("Nova transação recebida:");
                    System.out.println("Chave: " + record.key());
                    System.out.println("Valor: " + record.value());
                    System.out.println("Partição: " + record.partition());
                    System.out.println("Offset: " + record.offset());
                    // Aqui entrará a lógica de detecção de fraude
                }
            }
        }
    }
}
