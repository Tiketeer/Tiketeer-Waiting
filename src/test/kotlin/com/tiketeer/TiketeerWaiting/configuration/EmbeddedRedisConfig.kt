package com.tiketeer.TiketeerWaiting.configuration

import com.tiketeer.TiketeerWaiting.util.RedisExpirationListener
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.RedisKeyValueAdapter
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.data.redis.serializer.RedisSerializationContext
import redis.embedded.RedisServer
import java.io.IOException

private val logger = KotlinLogging.logger {}

@DisplayName("Embedded Redis 설정")
@Configuration
@Profile("test")
class EmbeddedRedisConfig {
    @Value("\${spring.data.redis.host}")
    private lateinit var host: String

    @Value("\${spring.data.redis.port}")
    private lateinit var port: Number

    @Value("\${spring.data.redis.maxmemory}")
    private lateinit var maxmemorySize: Number

    @Value("\${spring.data.redis.notify-keyspace-events}")
    private lateinit var keyspaceEvent: String

    private lateinit var redisServer: RedisServer

    @PostConstruct
    @Throws(IOException::class)
    fun startRedis() {
        this.redisServer = RedisServer.builder().port(port.toInt()).setting("maxmemory " + maxmemorySize + "M").setting("notify-keyspace-events $keyspaceEvent").build()
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

    @Bean
    fun redisMessageListenerContainer(connectionFactory: LettuceConnectionFactory) : RedisMessageListenerContainer{
        val listenerContainer = RedisMessageListenerContainer();
        listenerContainer.setConnectionFactory(connectionFactory);
        listenerContainer.setErrorHandler { e -> logger.error(e) { "There was an error in redis key expiration listener container" } };
        return listenerContainer;
    }
}