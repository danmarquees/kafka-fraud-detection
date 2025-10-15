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
