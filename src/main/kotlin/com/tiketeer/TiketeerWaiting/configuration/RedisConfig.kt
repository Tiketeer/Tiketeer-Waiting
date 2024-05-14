package com.tiketeer.TiketeerWaiting.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext

@Configuration
@Profile("!test")
class RedisConfig {
    @Value("\${spring.data.redis.host}")
    private lateinit var host: String

    @Value("\${spring.data.redis.port}")
    private lateinit var port: Number

    @Value("\${spring.data.redis.password}")
    private lateinit var password: String

    @Bean
    fun redisConnectionFactory() : LettuceConnectionFactory {
        val redisStandaloneConfiguration = RedisStandaloneConfiguration(host, port.toInt())
        redisStandaloneConfiguration.setPassword(password)
        return LettuceConnectionFactory(redisStandaloneConfiguration)
    }

    @Bean
    fun redisTemplate(connectionFactory: ReactiveRedisConnectionFactory) : ReactiveRedisTemplate<String, String> {
        return ReactiveRedisTemplate(connectionFactory, RedisSerializationContext.string())
    }

    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory?): RedisCacheManager {
        return RedisCacheManager.create(connectionFactory!!)
    }
}