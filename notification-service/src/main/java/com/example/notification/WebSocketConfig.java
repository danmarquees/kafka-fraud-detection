package com.example.notification;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Destinos que o servidor envia mensagens para os clientes (ex: /topic/frauds)
        config.enableSimpleBroker("/topic");
        // Prefixo para mensagens destinadas a métodos anotados com @MessageMapping (não usado aqui, mas bom ter)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // O endpoint que o cliente usará para se conectar ao nosso WebSocket
        registry.addEndpoint("/ws-alerts").withSockJS();
    }
}
