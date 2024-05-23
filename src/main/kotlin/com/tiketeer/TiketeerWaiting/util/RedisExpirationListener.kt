package com.tiketeer.TiketeerWaiting.util;

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.stereotype.Component
import org.springframework.util.ObjectUtils

private val logger = KotlinLogging.logger {}

@Component
class RedisExpirationListener @Autowired constructor(private val redisTemplate: ReactiveRedisTemplate<String, String>, listenerContainer: RedisMessageListenerContainer) : KeyExpirationEventMessageListener(listenerContainer) {
	private final val uuidPattern = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
	private final val emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"

	override fun doHandleMessage(message: Message) {
		val ttlKey = String(message.body)
		val regex = "^ttl::$uuidPattern::$emailPattern\$".toRegex()
		if(regex.matches(ttlKey)){
			val args = ttlKey.split("::")
			val key = "queue::${args[1]}"
			val token = args[2]
			redisTemplate.opsForZSet().remove(key, token).subscribe { _ -> logger.debug { "expired key: $ttlKey" } }
		}

	}
}