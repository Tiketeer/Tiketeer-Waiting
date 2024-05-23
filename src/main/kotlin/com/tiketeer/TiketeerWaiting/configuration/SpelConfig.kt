package com.tiketeer.TiketeerWaiting.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.expression.spel.standard.SpelExpressionParser

@Configuration
class SpelConfig {
	@Bean
	fun spelExpressionParser(): SpelExpressionParser {
		return SpelExpressionParser()
	}
}