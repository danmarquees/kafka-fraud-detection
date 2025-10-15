package com.example.producer;

import com.example.common.kafka.GsonSerializer;
import com.example.common.kafka.Transaction;
import java.math.BigDecimal;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public class TransactionProducer {

    public static final String KAFKA_TOPIC = "transactions";

    public static void main(String[] args) {
        Properties props = new Properties();
        // Endereço do nosso broker Kafka iniciado via Docker Compose
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9021");
        // Serializador da chave (String)
        props.put(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class.getName()
        );
        // Nosso serializador customizado para o valor (Transaction)
        props.put(
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            GsonSerializer.class.getName()
        );

        try (
            KafkaProducer<String, Transaction> producer = new KafkaProducer<>(
                props
            )
        ) {
            Random random = new Random();

            // Gera e envia uma transação a cada segundo
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> {
                    String userId = "user-" + random.nextInt(10); // Simula 10 usuários
                    String transactionId = UUID.randomUUID().toString();
                    // Gera um valor de transação entre 1.00 e 500.00
                    BigDecimal amount = new BigDecimal(
                        String.format("%.2f", 1 + (500 * random.nextDouble()))
                    );
                    long timestamp = System.currentTimeMillis();

                    Transaction transaction = new Transaction(
                        transactionId,
                        userId,
                        amount,
                        timestamp
                    );

                    // A chave da mensagem será o ID do usuário
                    ProducerRecord<String, Transaction> record =
                        new ProducerRecord<>(KAFKA_TOPIC, userId, transaction);

                    producer.send(record, (metadata, exception) -> {
                        if (exception == null) {
                            System.out.println(
                                "Transação enviada com sucesso: " + transaction
                            );
                        } else {
                            System.err.println(
                                "Erro ao enviar transação: " + transaction
                            );
                            exception.printStackTrace();
                        }
                    });
                },
                0,
                1,
                TimeUnit.SECONDS
            );

            // Mantém a aplicação rodando
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
