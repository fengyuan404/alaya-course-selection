package com.alaya.coursesystem.alaya_course_selection.config; // 替换为你的实际包名

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
 * Redis 缓存配置类（解决 LocalDateTime 序列化问题）
 */
@Configuration
@EnableCaching // 开启缓存注解支持（关键，若未加会导致缓存注解失效）
public class RedisConfig {

    /**
     * 自定义 ObjectMapper，注册 JSR310 模块支持 Java 8 日期类型
     */
    @Bean // 必须加 @Bean 注解，否则无法被 Spring 容器识别
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 1. 注册 Java 8 日期时间模块（核心：解决 LocalDateTime 序列化）
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        objectMapper.registerModule(javaTimeModule);

        // 2. 禁用日期转为时间戳（可选：转为 "2025-11-22T17:00:00" 格式字符串）
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 3. 可选：忽略未知字段（避免实体新增字段时序列化报错）
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

//        // ========== 新增：保留泛型类型信息（核心修复） ==========
//        objectMapper.activateDefaultTyping(
//                LaissezFaireSubTypeValidator.instance, // 宽松的类型校验器（允许所有子类）
//                ObjectMapper.DefaultTyping.NON_FINAL,  // 给非final类添加类型信息
//                JsonTypeInfo.As.PROPERTY // 类型信息作为JSON的一个属性（如"@class":"com.xxx.PageResponseVO"）
//        );

        return objectMapper;
    }



    /**
     * 配置 Redis 缓存管理器，指定序列化规则
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 1. Key 序列化：使用字符串（必须，Redis Key 不支持复杂类型）
        StringRedisSerializer keySerializer = new StringRedisSerializer();

        // 2. Value 序列化：使用自定义 ObjectMapper 的 JSON 序列化器
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(objectMapper());

        // 3. 缓存配置（过期时间、序列化规则）
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(2)) // 缓存过期时间（2小时）
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .disableCachingNullValues(); // 不缓存 null 值（避免缓存穿透）

        // 4. 构建缓存管理器
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }
}