# Sistema de detecção de fraudes em tempo real com Kafka e Java

  [span_0](start_span)[span_1](start_span) Este projeto implementa um sistema de baixa latência para a detecção de fraudes em transações financeiras, utilizando uma arquitetura orientada a eventos com Apache Kafka [span_0](end_span)[span_1](end_span). [span_2](start_span)O sistema analisa um fluxo contínuo de transações para dentificar padrões fraudulentos em tempo real.[span_2](end_span).

## 1. Configuração do Ambiente

A infraestrutura do Apache Kafka é gerenciada via Docker Compose para simplificar a configuração do ambiente de desenvolvimento.

### Pré-requisitos
- Java JDK 17+
- Docker e Docker Compose
- Apache Maven 3.8+

### Inicializção do Kafka
Para iniciar os serviços do Kafka e Zookeeper, execute o seguinte comando na raiz do projeto:

```bash
docker-compose up -d
```

[cite_start]Este comando irá iniciar os contâineres necessários em modo "detached (segundo plano)"[cite:61,62].

## 2. Estrutura do projeto

O projeto está organizado em um modelo Maven multi-módulo para garantir a separação de responsabilidades e o reuso de código.

- **'kafka-producer'**: Um serviço simples responsável por simular e enviar eventos de transação para o tópico Kafka.
- **'fraud-detector'**: O serviço principal que consome os eventos de transação, aplica a lógica de detecção de fraude em tempo real e emite alertas.
- **'common-kafka'**: Uma biblioteca compartilhada que contém código comum, com serializadores, desserializadores e modelos de dados utilizados tanto pelo produtor quanto pelo consumidor.

### Módulo `common-kafka`

Este módulo é fundamental para a coesão do projeto. Ele contém:

-   **Modelos de Dados:** Classes POJO (Plain Old Java Objects) que representam os eventos trafegados no Kafka, como a classe `Transaction`.
-   **Serializadores e Desserializadores Customizados:** Implementações para converter objetos Java em JSON (e vice-versa) para que possam ser enviados e recebidos pelo Kafka. Isso garante que o produtor e o consumidor compartilhem o mesmo schema de mensagem.

### Módulo `kafka-producer`

Este serviço Java tem a função de simular um fluxo contínuo de eventos de transações financeiras.

-   **Geração de Dados:** Cria transações com dados aleatórios (ID de usuário, valor, etc.) em intervalos regulares de tempo.
-   **Publicação no Kafka:** Envia cada transação gerada para o tópico `transactions`. O ID do usuário (`userId`) é usado como a chave da mensagem, o que garante que todas as transações de um mesmo usuário sejam processadas pelo mesmo consumidor, mantendo a ordem.

### Módulo `fraud-detector`

Este é o serviço principal do sistema, responsável por analisar o fluxo de transações e identificar atividades suspeitas.

-   **Consumidor Kafka:** Se inscreve no tópico `transactions` para receber os eventos em tempo real.
-   **Desserialização:** Utiliza um `GsonDeserializer` customizado para converter a mensagem JSON de volta para um objeto Java `Transaction`.
-   **Lógica de Fraude (Stateful):** Implementa uma regra de detecção baseada em janela de tempo. O serviço mantém um estado em memória que rastreia o número de transações por usuário. Se um usuário excede um limite de transações (ex: > 2) em um curto período (ex: 10 segundos), um alerta de fraude é gerado.
