# Distributed Notifications System with Cache-Aside Pattern

## Architecture

- **Config Server** (8888): Centralized configuration
- **Eureka Server** (8761): Service Discovery
- **API Gateway** (8080): Entry point
- **Users Service** (8081): User and preferences management + Redis Cache
- **Notifications Service** (8082): Notification sending

## Implemented Cache-Aside Pattern

The `users-service` caches user preferences in Redis:

- First query: DB (~50ms) + caching in Redis
- Subsequent queries: Redis (~1ms)
- Updates: DB + automatic cache update
- Deletions: DB + cache eviction

### Cache Synchronization Strategies

#### 1. **@Cacheable** - Cache-Aside Read

```java

@Cacheable(value = "user-preferences", key = "#userId", unless = "#result == null")
public UserPreferencesResponse getPreferencesByUserId(Long userId) {
    // First looks in Redis, if not exists queries DB and caches the result
    // Returns preferences from database
}
```

#### 2. **@CachePut** - Simple Update (V1)

```java

@CachePut(value = "user-preferences", key = "#userId")
public UserPreferencesResponse updatePreferences(Long userId, UpdateUserPreferencesRequest request) {
    // Updates DB and refreshes individual cache entry
    // Key: user-preferences::{userId}
    // WARNING: If you have getAllPreferences() cached, it will become stale
}
```

#### 3. **@CacheEvict** - Simple Deletion (V1)

```java

@CacheEvict(value = "user-preferences", key = "#userId")
public void deletePreferences(Long userId) {
    // Deletes from DB and evicts individual cache entry
    // Key: user-preferences::{userId}
    // WARNING: If you have getAllPreferences() cached, it will still show the deleted record
}
```

#### 4. **@Caching** - Multiple Operations (V2)

**Update V2 - Updates Individual + Evicts List:**

```java

@Caching(
        put = {
                @CachePut(value = "user-preferences", key = "#userId")
        },
        evict = {
                @CacheEvict(value = "user-preferences", key = "'all'")
        }
)
public UserPreferencesResponse updatePreferencesV2(Long userId, UpdateUserPreferencesRequest request) {
    // Updates individual entry: user-preferences::{userId}
    // Evicts complete list: user-preferences::all
    // Next call to getAllPreferences() will regenerate the updated list
}
```

**Delete V2 - Evicts Individual + List:**

```java

@Caching(
        evict = {
                @CacheEvict(value = "user-preferences", key = "#userId"),
                @CacheEvict(value = "user-preferences", key = "'all'")
        }
)
public void deletePreferencesV2(Long userId) {
    // Evicts individual entry: user-preferences::{userId}
    // Evicts complete list: user-preferences::all
    // Keeps both caches synchronized
}
```

**When to use each version?**

| Scenario                              | Recommended Version | Reason                    |
|---------------------------------------|---------------------|---------------------------|
| Only individual queries               | **V1**              | Simpler, fewer operations |
| You have `getAllPreferences()` cached | **V2**              | Keeps list synchronized   |
| Application with multiple query types | **V2**              | Avoids inconsistencies    |

**Advantages of @Caching:**

- Allows combining multiple cache operations (`@CachePut` + `@CacheEvict`) in a single transaction
- Avoids the complexity of manually updating lists in cache
- Strategy: **update individual, evict collections** (regenerated on next GET)

## Redis Configuration

### 1. Properties (Config Server - users-service.yml)

```yaml
# infrastructure/config-server/src/main/resources/config/users-service.yml
spring:
  # REDIS CONFIGURATION - Cache
  data:
    redis:
      host: localhost
      port: 6379
      password: redis123
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8      # Maximum active connections
          max-idle: 8        # Maximum idle connections
          min-idle: 2        # Minimum idle connections
          max-wait: 2000ms   # Maximum wait time to obtain connection
        shutdown-timeout: 100ms

  # CACHE CONFIGURATION
  cache:
    type: redis
    redis:
      time-to-live: 600000    # 10 minutes in milliseconds (default TTL)
      cache-null-values: false # Don't cache null values
      use-key-prefix: true
      key-prefix: "users-service:"  # Prefix for all keys

# LOGGING CONFIGURATION (optional for debugging)
logging:
  level:
    org.springframework.cache: DEBUG
    org.springframework.data.redis: DEBUG
```

### 2. Java Configuration - RedisConfig.java

```java
package com.mspoc.users_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;  // IMPORTANT
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching  // Enables cache support with annotations (@Cacheable, @CachePut, etc.)
public class RedisConfig {

    @Value("${spring.cache.redis.time-to-live}")
    private Integer redisTtlInMinutes;

    @Value("${spring.cache.redis.key-prefix}")
    private String cacheKeyPrefix;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {

        // Default configuration for all caches
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(redisTtlInMinutes))  // TTL from properties
                .disableCachingNullValues()                        // Don't cache nulls
                .prefixCacheNameWith(cacheKeyPrefix)               // Prefix "users-service:"
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))  // Keys as String
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper())));  // Values as JSON

        // Specific configurations per cache name (optional)
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("user-preferences",
                        defaultConfig.entryTtl(Duration.ofMinutes(5)))  // user-preferences: 5 min
                .withCacheConfiguration("user-profiles",
                        defaultConfig.entryTtl(Duration.ofMinutes(30))) // user-profiles: 30 min
                .withCacheConfiguration("notification-settings",
                        defaultConfig.entryTtl(Duration.ofMinutes(20))) // notification-settings: 20 min
                .build();
    }

    private ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());  // Support for LocalDateTime, ZonedDateTime, etc.
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // Dates as ISO-8601

        // DO NOT use activateDefaultTyping() to avoid problems with lists/arrays
        // Spring Cache knows the method return type, no polymorphic metadata needed

        return mapper;
    }
}
```

### 3. Key Configuration Points

**@EnableCaching:**

- **Required** to enable cache annotation support
- Must be on a `@Configuration` class
- Activates `@Cacheable`, `@CachePut`, `@CacheEvict`, `@Caching`

**Serialization:**

- **Keys**: `StringRedisSerializer` - Readable format in Redis
- **Values**: `GenericJackson2JsonRedisSerializer` - JSON in Redis
- **Avoid**: `activateDefaultTyping()` - Causes problems with lists

**Custom TTL:**

- You can configure different TTL per cache name
- `user-preferences`: 5 minutes (frequently changing data)
- `user-profiles`: 30 minutes (more stable data)

**Connection Pooling (Lettuce):**

- `max-active: 8` - Maximum 8 simultaneous connections
- `max-wait: 2000ms` - If no connection available, wait 2 seconds

## Starting the System

1. Start Docker infrastructure:

```bash
docker-compose up -d
```

2. Start services in order:

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

## Main Endpoints

### Users Service

```bash
# Create user
POST http://localhost:8081/users

# Create preferences
POST http://localhost:8081/preferences

# Get preferences (with cache)
GET http://localhost:8081/preferences/user/{userId}

# Get all preferences (with list cache)
GET http://localhost:8081/preferences

# Update preferences - V1 (only updates individual cache)
PUT http://localhost:8081/preferences/user/{userId}

# Update preferences - V2 (updates individual cache + evicts list)
PUT http://localhost:8081/v2/preferences/user/{userId}

# Delete preferences - V1 (only evicts individual cache)
DELETE http://localhost:8081/preferences/user/{userId}

# Delete preferences - V2 (evicts individual cache + list)
DELETE http://localhost:8081/v2/preferences/user/{userId}
```

**Differences between V1 and V2:**

- **V1**: Only operates on individual cache entry (`user-preferences::{userId}`)
- **V2**: Operates on individual entry + complete list (`user-preferences::all`) using `@Caching`

### Notifications Service

```bash
# Check if notification can be sent
GET http://localhost:8082/notifications/can-send?userId=1&channel=EMAIL

# Send notification
POST http://localhost:8082/notifications/send?userId=1&channel=EMAIL&message=Test
```

## Notification Flow

1. Notification Service receives send request
2. Queries preferences from Users Service via Feign Client
3. Users Service returns preferences (from Redis if cached)
4. Notification Service validates:
    - Channel enabled
    - Not in quiet hours
5. Sends or discards the notification

## Monitoring

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

### Redis Cache Keys

```bash
# View all preference keys
KEYS "user-preferences::*"

# Examples of generated keys:
# user-preferences::1          -> Preferences for user ID 1
# user-preferences::2          -> Preferences for user ID 2  
# user-preferences::all        -> Complete list of all preferences

# View content of a specific key
GET "user-preferences::1"

# View TTL of a key (time to live)
TTL "user-preferences::1"

# Delete a specific key
DEL "user-preferences::1"

# Delete all preference keys
KEYS "user-preferences::*" | xargs redis-cli -a redis123 DEL
```

### Cache Behavior by Operation

| Operation                                   | Service Method             | Redis Effect                                                      |
|---------------------------------------------|----------------------------|-------------------------------------------------------------------|
| **GET** `/preferences/user/{id}`            | `getPreferencesByUserId()` | Reads `user-preferences::{id}`, caches if not exists              |
| **GET** `/preferences`                      | `getAllPreferences()`      | Reads `user-preferences::all`, caches if not exists               |
| **POST** `/preferences`                     | `createPreferences()`      | Creates `user-preferences::{id}`                                  |
| **PUT** `/preferences/user/{id}` (V1)       | `updatePreferences()`      | Updates `user-preferences::{id}`                                  |
| **PUT** `/v2/preferences/user/{id}` (V2)    | `updatePreferencesV2()`    | Updates `user-preferences::{id}` + Evicts `user-preferences::all` |
| **DELETE** `/preferences/user/{id}` (V1)    | `deletePreferences()`      | Evicts `user-preferences::{id}`                                   |
| **DELETE** `/v2/preferences/user/{id}` (V2) | `deletePreferencesV2()`    | Evicts `user-preferences::{id}` + Evicts `user-preferences::all`  |

**Legend:**

- **Updates**: Refreshes the value in cache with `@CachePut`
- **Evicts**: Removes the entry from cache with `@CacheEvict`
- **V1**: Operates only on individual cache
- **V2**: Operates on individual cache + list using `@Caching`

## Cache-Aside Advantages

- **Performance**: 50x faster on repeated queries
- **Scalability**: Reduces PostgreSQL load
- **Availability**: Continues working if Redis fails (queries DB)
- **Consistency**: Automatic cache updates

## Technologies

- Java 21
- Spring Boot 3.5.9
- Spring Cloud 2025.0.1
- Redis 7.4
- PostgreSQL 16
- MapStruct 1.6.3
- Lombok 1.18.30
- Flyway
- OpenFeign
