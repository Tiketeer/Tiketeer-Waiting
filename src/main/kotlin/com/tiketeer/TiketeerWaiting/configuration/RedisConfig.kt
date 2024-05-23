package com.tiketeer.TiketeerWaiting.configuration


import com.tiketeer.TiketeerWaiting.util.AspectUtils
import com.tiketeer.TiketeerWaiting.util.RedisExpirationListener
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.RedisKeyValueAdapter
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.expression.spel.standard.SpelExpressionParser

private val logger = KotlinLogging.logger {}
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
    fun redisMessageListenerContainer(connectionFactory: RedisConnectionFactory) : RedisMessageListenerContainer{
        val listenerContainer = RedisMessageListenerContainer();
        listenerContainer.setConnectionFactory(connectionFactory);
//        listenerContainer.addMessageListener(expirationListener, PatternTopic("__keyevent@*__:expired"));
        listenerContainer.setErrorHandler { e -> logger.error(e) { "There was an error in redis key expiration listener container" } };
        return listenerContainer;
    }
}