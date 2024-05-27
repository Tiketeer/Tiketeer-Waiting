package com.tiketeer.TiketeerWaiting.util;

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.listener.Topic
import org.springframework.stereotype.Component
import org.springframework.util.ObjectUtils

private val logger = KotlinLogging.logger {}

@Component
class RedisExpirationListener @Autowired constructor(private val redisTemplate: ReactiveRedisTemplate<String, String>, listenerContainer: RedisMessageListenerContainer) : MessageListener {
	private final val keyEventExpiredTopic: Topic = PatternTopic("__keyevent@*__:expired")
	private final val uuidPattern = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
	private final val emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"

	init {
		listenerContainer.addMessageListener(this, keyEventExpiredTopic)
	}

	override fun onMessage(message: Message, pattern: ByteArray?) {
		if (ObjectUtils.isEmpty(message.channel) || ObjectUtils.isEmpty(message.body)) {
			return
		}
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