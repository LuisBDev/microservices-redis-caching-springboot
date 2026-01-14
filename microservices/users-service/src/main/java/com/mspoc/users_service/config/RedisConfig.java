package com.mspoc.users_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Configuración de Redis para Cache-Aside Pattern
 * 
 * Esta configuración habilita el caché distribuido usando Redis como backend.
 * Implementa el patrón Cache-Aside donde:
 * 1. La aplicación consulta primero el caché
 * 2. Si no existe (cache miss), consulta la BD
 * 3. Guarda el resultado en caché para futuras consultas
 * 
 * @author Luis Balarezo
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Configura el CacheManager para usar Redis como backend de caché.
     * 
     * Características:
     * - TTL configurable por caché
     * - Serialización JSON para objetos complejos
     * - Prefijos para namespacing
     * - Deshabilitación de caché de valores nulos
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {

        // Configuración por defecto para todos los cachés
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // TTL por defecto: 10 minutos
                .disableCachingNullValues() // No cachear valores null
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer(objectMapper())));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                // Configuraciones específicas por caché
                .withCacheConfiguration("user-preferences",
                        defaultConfig.entryTtl(Duration.ofMinutes(15))) // 15 min para preferencias
                .withCacheConfiguration("user-profiles",
                        defaultConfig.entryTtl(Duration.ofMinutes(30))) // 30 min para perfiles
                .withCacheConfiguration("notification-settings",
                        defaultConfig.entryTtl(Duration.ofMinutes(20))) // 20 min para settings
                .build();
    }

    /**
     * ObjectMapper personalizado para serialización JSON en Redis.
     * Incluye soporte para Java 8+ Date/Time API.
     */
    private ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.findAndRegisterModules();
        return mapper;
    }
}
