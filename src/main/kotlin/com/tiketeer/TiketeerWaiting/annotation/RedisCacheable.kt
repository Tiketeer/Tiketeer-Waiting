package com.tiketeer.TiketeerWaiting.annotation


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RedisCacheable(
	val key: String = "",
	val value: String
)
