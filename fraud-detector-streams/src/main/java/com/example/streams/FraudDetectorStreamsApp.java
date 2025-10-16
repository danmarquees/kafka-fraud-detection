package com.example.streams;

import com.example.common.kafka.GsonDeserializer;
import com.example.common.kafka.GsonSerializer;
import com.example.common.kafka.Transaction;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

import java.time.Duration;
import java.util.Properties;

public class FraudDetectorStreamsApp {

    public static final String INPUT_TOPIC = "transactions";
    public static final String OUTPUT_TOPIC = "fraud_alerts";
    private static final int TRANSACTION_LIMIT = 2;
    private static final Duration TIME_WINDOW = Duration.ofSeconds(10);

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "fraud-detector-streams-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        // Serde customizado para o objeto Transaction
        Serde<Transaction> transactionSerde = Serdes.serdeFrom(new GsonSerializer<>(), new GsonDeserializer<>(Transaction.class));

        StreamsBuilder builder = new StreamsBuilder();

        // 1. Ler do tópico de entrada
        KStream<String, Transaction> transactionStream = builder.stream(INPUT_TOPIC, Consumed.with(Serdes.String(), transactionSerde));

        transactionStream
                // 2. Agrupar pelo ID do usuário (a chave da mensagem)
                .groupByKey()
                // 3. Definir uma janela de tempo "tombamento" (tumbling window) de 10 segundos
                .windowedBy(TimeWindows.ofSizeWithNoGrace(TIME_WINDOW))
                // 4. Contar as ocorrências por chave dentro de cada janela
                .count()
                // 5. Filtrar apenas as janelas onde a contagem excedeu o limite
                .filter((windowedUserId, count) -> count > TRANSACTION_LIMIT)
                // 6. Converter o resultado de volta para um stream
                .toStream()
                // 7. Imprimir no console (para depuração) e preparar para re-publicar
                .peek((windowedUserId, count) -> System.err.println(
                        "!!!! FRAUDE DETECTADA !!!! Usuário: " + windowedUserId.key() +
                                " realizou " + count + " transações na janela de " + TIME_WINDOW.toSeconds() + "s"
                ))
                // 8. Mapear a chave para ser uma String simples novamente (userId)
                .selectKey((windowedUserId, count) -> windowedUserId.key())
                // O valor não importa para o tópico de alertas, então o definimos como nulo
                // Em um caso real, poderíamos enviar a lista de transações fraudulentas
                .mapValues((readOnlyKey, value) -> (Transaction) null)
                // 9. Enviar o resultado para o tópico de alertas
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), transactionSerde));


        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        // Adiciona um hook para fechar a aplicação de forma limpa
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        streams.start();
    }
}
