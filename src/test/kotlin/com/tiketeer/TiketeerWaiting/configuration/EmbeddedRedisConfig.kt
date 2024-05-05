package com.tiketeer.TiketeerWaiting.configuration

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import redis.embedded.RedisServer
import java.io.IOException

private val logger = KotlinLogging.logger {}

@DisplayName("Embedded Redis 설정")
@TestConfiguration
class EmbeddedRedisConfig {
    @Value("\${spring.data.redis.host}")
    private lateinit var host: String

    @Value("\${spring.data.redis.port}")
    private lateinit var port: Number

    @Value("\${spring.redis.maxmemory}")
    private lateinit var maxmemorySize: Number

    private lateinit var redisServer: RedisServer

    @PostConstruct
    @Throws(IOException::class)
    fun startRedis() {
        this.redisServer = RedisServer.builder().port(port.toInt()).setting("maxmemory " + maxmemorySize + "M").build()
        try {
            this.redisServer.start()
            logger.info {"레디스 서버 시작 성공" }
        } catch (e: Exception) {
            logger.error (e) { "레디스 서버 시작 실패: ${e.message}" }
        }
    }

    @PreDestroy
    fun stopRedis() {
        redisServer.stop()
    }

    @Bean
    fun redisConnectionFactory() : LettuceConnectionFactory {
        val redisStandaloneConfiguration = RedisStandaloneConfiguration(host, port.toInt())
        return LettuceConnectionFactory(redisStandaloneConfiguration)
    }

    @Bean
    fun redisTemplate(connectionFactory: ReactiveRedisConnectionFactory) : ReactiveRedisTemplate<String, String> {
        return ReactiveRedisTemplate(connectionFactory, RedisSerializationContext.string())
    }
}