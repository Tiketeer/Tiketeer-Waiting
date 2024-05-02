package com.tiketeer.TiketeerWaiting.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext

@Configuration
class RedisConfig {
    @Value("\${spring.redis.host}")
    private lateinit var host: String

    @Value("\${spring.redis.port}")
    private lateinit var port: Number

    @Bean
    fun redisConnectionFactory() : LettuceConnectionFactory {
        return LettuceConnectionFactory(host, port.toInt())
    }

    @Bean
    fun redisTemplate(connectionFactory: ReactiveRedisConnectionFactory) : ReactiveRedisTemplate<String, String> {
        return ReactiveRedisTemplate<String, String>(connectionFactory, RedisSerializationContext.string())
    }
}