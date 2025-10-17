# Sistema de Detecção de Fraudes em Tempo Real com Kafka e Java

Este projeto implementa um sistema de baixa latência para a detecção de fraudes em transações financeiras. Utilizando uma arquitetura de microsserviços orientada a eventos com Apache Kafka, o sistema processa um fluxo contínuo de transações, identifica atividades fraudulentas usando o Kafka Streams, notifica um dashboard em tempo real via WebSockets e persiste os alertas em um banco de dados PostgreSQL.

## Arquitetura do Sistema

O fluxo de dados segue a seguinte arquitetura:

1.  **`kafka-producer`**: Simula transações financeiras e as publica no tópico `transactions`.
2.  **`fraud-detector-streams`**: Consome o tópico `transactions`, aplica uma lógica de detecção de fraude com estado (janelas de tempo) e, ao encontrar uma fraude, publica um alerta no tópico `fraud_alerts`.
3.  **Tópico `fraud_alerts` (Fan-Out)**: Atua como um ponto central para distribuir os alertas para múltiplos serviços.
      * **`persistence-service`**: Consome os alertas e os salva no banco de dados **PostgreSQL** para auditoria e análise.
      * **`notification-service`**: Consome os alertas e os envia via **WebSocket** para o dashboard web.
4.  **Interface (Dashboard)**: Uma página web simples que se conecta ao `notification-service` e exibe os alertas de fraude assim que eles ocorrem.
5.  **Infraestrutura Docker**: Todos os serviços de backend (Kafka, Zookeeper, PostgreSQL, Adminer) são gerenciados via `docker-compose.yml`.

## Módulos do Projeto

O projeto é organizado em um modelo Maven multi-módulo:

  * **`common-kafka`**: Biblioteca compartilhada contendo o modelo de dados (`Transaction.java`) e os serializadores/desserializadores customizados (Gson).
  * **`kafka-producer`**: Serviço Java que simula e publica eventos de transação no tópico `transactions`.
  * **`fraud-detector-streams`**: Serviço de processamento de stream que utiliza a biblioteca **Kafka Streams** para aplicar a lógica de detecção de fraude. Ele agrupa transações por usuário em janelas de 10 segundos e filtra usuários que excedem 2 transações.
  * **`notification-service`**: Um microsserviço Spring Boot que consome do tópico `fraud_alerts` e retransmite os alertas para a interface do usuário via WebSocket.
  * **`persistence-service`**: Um microsserviço Spring Boot com Spring Data JPA que consome do tópico `fraud_alerts` e persiste os dados da transação fraudulenta em um banco de dados PostgreSQL.

## Tecnologias Utilizadas

  * **Linguagem:** Java 17
  * **Mensageria/Stream:** Apache Kafka
  * **Processamento de Stream:** Kafka Streams
  * **Backend:** Spring Boot (para os serviços de notificação e persistência)
  * **Banco de Dados:** PostgreSQL
  * **Comunicação Real-time (UI):** WebSockets (via STOMP)
  * **Frontend:** HTML, CSS (Bootstrap), JavaScript (SockJS, StompJS)
  * **Containerização:** Docker e Docker Compose
  * **Build:** Apache Maven

## Pré-requisitos

Para executar este projeto, você precisará ter instalados:

  * Java JDK 17+
  * Apache Maven 3.8+
  * Docker
  * Docker Compose

## Como Executar o Projeto

Siga os passos abaixo para iniciar o sistema completo.

### 1\. Iniciar a Infraestrutura de Backend

Inicie todos os contêineres Docker (Kafka, Zookeeper, PostgreSQL e Adminer). Execute este comando na raiz do projeto:

```bash
docker-compose up -d
```

### 2\. Compilar todo o Projeto

Compile todos os módulos Maven para garantir que todas as dependências estejam resolvidas e as classes construídas.

```bash
mvn clean install
```

### 3\. Iniciar os Microsserviços

Abra **quatro terminais separados** na raiz do projeto e execute um serviço em cada um. A ordem de inicialização é importante.

**Terminal 1: Serviço de Persistência**

```bash
mvn exec:java -pl persistence-service -Dexec.mainClass="com.example.persistence.PersistenceServiceApplication"
```

**Terminal 2: Serviço de Notificação (Dashboard)**

```bash
mvn exec:java -pl notification-service -Dexec.mainClass="com.example.notification.NotificationServiceApplication"
```

**Terminal 3: Detector de Fraudes (Kafka Streams)**

```bash
mvn exec:java -pl fraud-detector-streams -Dexec.mainClass="com.example.streams.FraudDetectorStreamsApp"
```

**Terminal 4: Produtor de Transações (Inicie por último)**
*Aguarde os outros serviços iniciarem antes de rodar este.*

```bash
mvn exec:java -pl kafka-producer -Dexec.mainClass="com.example.producer.TransactionProducer"
```

### 4\. Acessar os Dashboards

Com tudo rodando, você pode monitorar o sistema:

  * **Dashboard de Fraudes:** Abra seu navegador e acesse **`http://localhost:8081`**.

      * A página exibirá os alertas de fraude em tempo real assim que o `kafka-producer` os gerar e o `fraud-detector-streams` os processar.

  * **Adminer (Gerenciador do Banco de Dados):** Acesse **`http://localhost:8080`**.

      * Use as seguintes credenciais para logar:
          * **Sistema:** `PostgreSQL`
          * **Servidor:** `postgres-db`
          * **Usuário:** `frauduser`
          * **Senha:** `fraudpass`
          * **Base de dados:** `fraud_detection_db`
      * Ao clicar na tabela `fraudulent_transaction`, você verá o histórico de todas as fraudes que foram persistidas.
