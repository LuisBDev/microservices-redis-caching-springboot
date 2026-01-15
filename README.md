# Sistema de Notificaciones Distribuidas con Cache-Aside Pattern

## Arquitectura

- **Config Server** (8888): Configuración centralizada
- **Eureka Server** (8761): Service Discovery
- **API Gateway** (8080): Puerta de entrada
- **Users Service** (8081): Gestión de usuarios y preferencias + Redis Cache
- **Notifications Service** (8082): Envío de notificaciones

## Patrón Cache-Aside Implementado

El `users-service` cachea las preferencias de usuario en Redis:
- Primera consulta: BD (~50ms) + cacheo en Redis
- Siguientes consultas: Redis (~1ms)
- Actualizaciones: BD + actualización automática del caché
- Eliminaciones: BD + evicción del caché

## Configuración de Redis

```yaml
# infrastructure/config-server/src/main/resources/config/users-service.yml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: redis123
  cache:
    type: redis
    redis:
      time-to-live: 600000
```

## Iniciar el Sistema

1. Levantar infraestructura Docker:
```bash
docker-compose up -d
```

2. Iniciar servicios en orden:
```bash
# Config Server
cd infrastructure/config-server
mvn spring-boot:run

# Eureka Server
cd infrastructure/eureka-server
mvn spring-boot:run

# Users Service
cd microservices/users-service
mvn spring-boot:run

# Notifications Service
cd microservices/notifications-service
mvn spring-boot:run

# API Gateway
cd infrastructure/api-gateway
mvn spring-boot:run
```

## Endpoints Principales

### Users Service
```bash
# Crear usuario
POST http://localhost:8081/users

# Crear preferencias
POST http://localhost:8081/preferences

# Obtener preferencias (con caché)
GET http://localhost:8081/preferences/user/{userId}

# Actualizar preferencias (actualiza caché)
PUT http://localhost:8081/preferences/user/{userId}
```

### Notifications Service
```bash
# Verificar si puede enviar notificación
GET http://localhost:8082/notifications/can-send?userId=1&channel=EMAIL

# Enviar notificación
POST http://localhost:8082/notifications/send?userId=1&channel=EMAIL&message=Test
```

## Flujo de Notificación

1. Notification Service recibe solicitud de envío
2. Consulta preferencias al Users Service vía Feign Client
3. Users Service retorna preferencias (desde Redis si está cacheado)
4. Notification Service valida:
   - Canal habilitado
   - No está en horario de silencio
5. Envía o descarta la notificación

## Monitoreo

```bash
# RedisInsight (GUI)
http://localhost:5540

# Health checks
http://localhost:8081/actuator/health
http://localhost:8082/actuator/health

# Redis CLI
docker exec -it redis-cache redis-cli -a redis123
KEYS users-service:user-preferences::*
```

## Ventajas del Cache-Aside

- **Performance**: 50x más rápido en consultas repetidas
- **Escalabilidad**: Reduce carga en PostgreSQL
- **Disponibilidad**: Continúa funcionando si Redis falla (consulta BD)
- **Consistencia**: Actualizaciones automáticas del caché

## Tecnologías

- Java 21
- Spring Boot 3.5.9
- Spring Cloud 2025.0.1
- Redis 7.4
- PostgreSQL 16
- MapStruct 1.6.3
- Lombok 1.18.30
- Flyway
- OpenFeign
