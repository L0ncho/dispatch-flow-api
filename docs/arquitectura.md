# Arquitectura del sistema — Guías de despacho

Este documento describe cómo se conectan los microservicios, RabbitMQ y los almacenamientos en el flujo asíncrono de creación de guías.

## Vista general de componentes

```mermaid
flowchart TB
    subgraph clients [Entrada]
        Client[Cliente / Postman]
        ApiGw[API Gateway AWS]
    end

    subgraph messaging [Mensajería]
        Exchange[dispatch.exchange]
        Q1["Cola 1: guide.created.queue"]
        DLQ["Cola 2 / DLQ: guide.created.dlq"]
    end

    subgraph producerMS ["MS Productor — producer :8080"]
        GuideCtrl[GuideController]
        AcceptUC[AcceptGuideRequestUseCase]
        Publisher[RabbitMQGuidePublisher]
        CrudUC[Casos de uso CRUD legacy]
    end

    subgraph consumerMS ["MS Consumidor — dispatch-flow-consumer :8081"]
        Listener["GuideMessageListener @RabbitListener"]
        ProcessUC[ProcessGuideMessageUseCase]
        PdfS3[PDF + EFS + S3]
    end

    subgraph data [Persistencia y archivos]
        OracleCrud[("dispatch_guides\nCRUD síncrono")]
        OracleAsync[("async_dispatch_guides\nprocesamiento async")]
        S3Bucket[(AWS S3 / LocalStack)]
        EfsStore[(EFS)]
    end

    Client --> ApiGw
    ApiGw --> GuideCtrl
    GuideCtrl -->|POST /api/guides| AcceptUC
    AcceptUC --> Publisher
    Publisher --> Exchange
    Exchange -->|routingKey guide.created| Q1
    Q1 --> Listener
    Listener --> ProcessUC
    ProcessUC --> PdfS3
    PdfS3 --> S3Bucket
    PdfS3 --> EfsStore
    ProcessUC --> OracleAsync

    GuideCtrl -->|GET PUT DELETE| CrudUC
    CrudUC --> OracleCrud
    CrudUC --> S3Bucket
    CrudUC --> EfsStore

    Q1 -.->|fallo tras reintentos| DLQ
    Exchange -.-> DLQ
```

## Flujo 1 — Crear guía (asíncrono)

Responsabilidad del **productor**: recibir, publicar y responder. No genera PDF ni persiste la guía final.

```mermaid
sequenceDiagram
    participant C as Cliente
    participant GW as API Gateway
    participant P as MS Productor
    participant RMQ as RabbitMQ Cola1
    participant Cn as MS Consumidor
    participant S3 as S3
    participant O as Oracle async_dispatch_guides

    C->>GW: POST /api/guides
    GW->>P: solicitud JSON
    P->>RMQ: GuideCreationMessage + trackingId
    P-->>C: 202 ACCEPTED

    RMQ->>Cn: mensaje automático @RabbitListener
    Cn->>Cn: validar + crear DispatchGuide
    Cn->>Cn: generar PDF
    Cn->>S3: subir PDF
    Cn->>O: guardar metadata + s3Key
```

## Flujo 2 — Error y DLQ

```mermaid
sequenceDiagram
    participant RMQ as RabbitMQ Cola1
    participant Cn as MS Consumidor
    participant DLQ as RabbitMQ Cola2 DLQ

    RMQ->>Cn: mensaje
    Cn->>Cn: procesar
    Note over Cn: Error PDF / S3 / Oracle / validación
    Cn->>Cn: retry hasta 3 intentos
    Cn->>RMQ: reject sin requeue
    RMQ->>DLQ: x-dead-letter-exchange
```

## Estructura del repositorio (Maven multi-módulo)

```mermaid
flowchart LR
    subgraph modules [dispatch-flow-parent]
        Shared["guides-shared\nDominio, GuideCreationMessage,\nRabbitMqTopology"]
        Prod["producer\nMS Productor Spring Boot"]
        Cons["dispatch-flow-consumer\nMS Consumidor Spring Boot"]
    end

    Shared --> Prod
    Shared --> Cons
    Prod -->|publica| RMQ[RabbitMQ]
    Cons -->|consume| RMQ
```

| Módulo | Artefacto | Puerto | Rol |
|--------|-----------|--------|-----|
| `guides-shared` | librería JAR | — | Contratos y dominio compartido |
| `producer` | `dispatch-flow-api` | 8080 | API REST, publicador RabbitMQ, CRUD legacy |
| `dispatch-flow-consumer` | `dispatch-flow-consumer` | 8081 | Listener, procesamiento, S3, tabla async |

## Topología RabbitMQ

| Recurso | Nombre |
|---------|--------|
| Exchange | `dispatch.exchange` (direct) |
| Cola principal | `guide.created.queue` (durable) |
| DLQ | `guide.created.dlq` (durable) |
| Routing key principal | `guide.created.routingKey` |
| Routing key DLQ | `guide.created.dlqRoutingKey` |

La configuración se declara en Java (`RabbitMQConfig`) al arrancar cada microservicio. No hay endpoints HTTP para crear colas.

## Separación de responsabilidades

| Acción | MS Productor | MS Consumidor |
|--------|:------------:|:-------------:|
| Recibir POST /api/guides | ✓ | |
| Publicar en Cola 1 | ✓ | |
| Responder 202 ACCEPTED | ✓ | |
| Escuchar Cola 1 | | ✓ |
| Generar PDF | | ✓ |
| Subir S3 | | ✓ |
| Guardar en `async_dispatch_guides` | | ✓ |
| CRUD sobre `dispatch_guides` | ✓ | |
| Enviar fallos a DLQ | | ✓ |

## Despliegue local

```bash
docker compose up -d          # RabbitMQ + LocalStack
./run-local                   # productor :8080
./run-consumer                # consumidor :8081
```

Consola RabbitMQ: http://localhost:15672 (credenciales en `.env` o `guest`/`guest`).
