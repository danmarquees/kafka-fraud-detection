package com.example.persistence;

import com.example.common.kafka.Transaction;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class FraudAlertConsumer {

    private final FraudRepository repository;

    public FraudAlertConsumer(FraudRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "fraud_alerts", groupId = "persistence-group")
    public void consume(Transaction fraudulentTx) {
        if (fraudulentTx == null) {
            System.out.println(
                "Mensagem de fraude nula recebida do Kafka Streams, ignorando."
            );
            return;
        }
        System.out.println(
            "Alerta de fraude recebido, persistindo no banco de dados: " +
                fraudulentTx.getTransactionId()
        );
        FraudulentTransaction entity = new FraudulentTransaction();
        // Copiar os dados do objeto Kafka para a entidade JPA
        // (getters e setters necessários na entidade)
        repository.save(entity);
    }
}
