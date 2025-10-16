package com.example.notification;

import com.example.common.kafka.Transaction;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class FraudAlertConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    // O Spring injeta o template de mensagens para nós
    public FraudAlertConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "fraud_alerts", groupId = "notification-group")
    public void consume(Transaction fraudTransaction) {
        System.out.println(
            "Alerta de fraude recebido: " + fraudTransaction.getTransactionId()
        );
        // Envia a transação recebida para o destino WebSocket "/topic/frauds"
        messagingTemplate.convertAndSend("/topic/frauds", fraudTransaction);
        System.out.println("Alerta enviado via WebSocket para o dashboard.");
    }
}
