package com.tiketeer.TiketeerWaiting.aspect

import com.fasterxml.jackson.databind.ObjectMapper
import com.tiketeer.TiketeerWaiting.annotation.RedisCacheable
import io.github.oshai.kotlinlogging.KotlinLogging
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.stream.Collectors
import com.tiketeer.TiketeerWaiting.util.AspectUtils

private val logger = KotlinLogging.logger {}

@Aspect
@Component
class RedisCacheAspect(
	private val redisTemplate: ReactiveRedisTemplate<String, String>,
	private val objectMapper: ObjectMapper,
	private val aspectUtils: AspectUtils) {
	@Around("execution(public * *(..)) && @annotation(com.tiketeer.TiketeerWaiting.annotation.RedisCacheable)")
	fun redisReactiveCacheable(joinPoint: ProceedingJoinPoint): Any {
		val methodSignature = joinPoint.signature as MethodSignature
		val method = methodSignature.method

		val returnType = method.returnType
		val annotation = method.getAnnotation(RedisCacheable::class.java)

		val key = aspectUtils.resolveKey(joinPoint, annotation.key, annotation.value)

		val typeReference = aspectUtils.getTypeReference(method)

		logger.info { "Evaluated Redis cacheKey: $key" }

		val cachedValue = redisTemplate.opsForValue().get(key)

		if (returnType.isAssignableFrom(Mono::class.java)) {
			return cachedValue
				.map { v ->
					objectMapper.readValue(v, typeReference)
				}
				.switchIfEmpty(Mono.defer {
					(joinPoint.proceed(joinPoint.args) as Mono<*>)
						.map { t ->
							redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(t)).subscribe()
							t
						}
				})
		} else if (returnType.isAssignableFrom(Flux::class.java)) {
			return cachedValue
				.flatMapMany { v ->
					Flux.fromIterable(
						(v as List<String>).stream()
							.map { e -> objectMapper.readValue(e, typeReference) }
							.collect(Collectors.toList()) as List<*>
					)

				}
				.switchIfEmpty(Flux.defer {
					(joinPoint.proceed(joinPoint.args) as Flux<*>)
						.collectList()
						.map { t ->
							redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(t)).subscribe()
							t
						}
						.flatMapMany { Flux.fromIterable(it) }
				})
		}

		throw RuntimeException("RedisReactiveCacheGet: Annotated method has unsupported return type, expected Mono<?> or Flux<?>")
	}
}